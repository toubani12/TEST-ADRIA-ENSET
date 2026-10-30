package toubani.badr.chatbotservice.fraud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Partie H : détection de fraude SANS entraînement local. Le LLM (Groq via Spring AI) reçoit des exemples
 * étiquetés tirés de bank_transactions.csv (few-shot) et les statistiques du jeu, puis classe une transaction.
 */
@Service
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    public record Tx(String id, double amount, int hour, String country, String merchantCategory,
                     int transactionsLast24h, double averageAmountLast7d, Integer isFraud) {
        String describe() {
            return "amount=%.2f, hour=%d, country=%s, merchant_category=%s, transactions_last_24h=%d, average_amount_last_7d=%.2f"
                    .formatted(amount, hour, country, merchantCategory, transactionsLast24h, averageAmountLast7d);
        }
    }

    public record FraudVerdict(boolean isFraud, double probability, String reason) {
    }

    public record Evaluation(int samples, int tp, int fp, int tn, int fn, double precision, double recall, double f1) {
    }

    private final ChatClient chat;
    private final List<Tx> data;
    private final String context;

    public FraudDetectionService(ChatClient.Builder builder,
                                 @Value("${bank.fraud.csv:bank_transactions.csv}") String csv) throws Exception {
        this.chat = builder.build();
        this.data = load(Path.of(csv));
        List<Tx> frauds = data.stream().filter(t -> t.isFraud() == 1).limit(8).toList();
        List<Tx> normals = data.stream().filter(t -> t.isFraud() == 0).limit(12).toList();
        long nbFraud = data.stream().filter(t -> t.isFraud() == 1).count();
        StringBuilder sb = new StringBuilder("Jeu bank_transactions.csv : %d transactions, %.2f %% de fraudes (classes déséquilibrées).\nExemples étiquetés :\n"
                .formatted(data.size(), 100.0 * nbFraud / Math.max(1, data.size())));
        frauds.forEach(t -> sb.append("- ").append(t.describe()).append(" -> is_fraud=1\n"));
        normals.forEach(t -> sb.append("- ").append(t.describe()).append(" -> is_fraud=0\n"));
        this.context = sb.toString();
        log.info("Détection de fraude : {} transactions chargées depuis {}", data.size(), csv);
    }

    public FraudVerdict score(Tx tx) {
        FraudVerdict v = chat.prompt()
                .system("""
                        Tu es un analyste anti-fraude bancaire. Compare la transaction aux exemples étiquetés
                        (montant très supérieur à la moyenne 7 jours, heure nocturne, rafale de transactions, pays ou
                        catégorie inhabituels). Réponds uniquement avec le JSON demandé, reason en français et courte.
                        """ + context)
                .user("Transaction à classer : " + tx.describe())
                .call().entity(FraudVerdict.class);
        return v;
    }

    /** Évalue le LLM sur un échantillon stratifié du CSV (hors exemples du prompt) : précision, rappel, F1. */
    public Evaluation evaluate(int n) {
        List<Tx> frauds = data.stream().filter(t -> t.isFraud() == 1).skip(8).limit(Math.max(1, n / 2)).toList();
        List<Tx> normals = data.stream().filter(t -> t.isFraud() == 0).skip(12).limit(Math.max(1, n - frauds.size())).toList();
        int tp = 0, fp = 0, tn = 0, fn = 0;
        for (Tx t : concat(frauds, normals)) {
            boolean predicted = score(t).isFraud();
            if (predicted && t.isFraud() == 1) tp++;
            else if (predicted) fp++;
            else if (t.isFraud() == 1) fn++;
            else tn++;
        }
        double p = tp + fp == 0 ? 0 : (double) tp / (tp + fp);
        double r = tp + fn == 0 ? 0 : (double) tp / (tp + fn);
        double f1 = p + r == 0 ? 0 : 2 * p * r / (p + r);
        return new Evaluation(tp + fp + tn + fn, tp, fp, tn, fn, p, r, f1);
    }

    public List<Tx> sample(int n) {
        return data.stream().limit(n).toList();
    }

    private static List<Tx> concat(List<Tx> a, List<Tx> b) {
        List<Tx> all = new ArrayList<>(a);
        all.addAll(b);
        return all;
    }

    private static List<Tx> load(Path csv) throws Exception {
        if (!Files.exists(csv)) {
            log.warn("CSV {} introuvable, détection de fraude sans exemples", csv);
            return List.of();
        }
        List<String> lines = Files.readAllLines(csv);
        List<String> header = Arrays.asList(lines.getFirst().trim().split(","));
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < header.size(); i++) idx.put(header.get(i).trim(), i);
        List<Tx> out = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            String[] c = line.trim().split(",", -1);
            if (c.length < header.size()) continue;
            try {
                out.add(new Tx(c[idx.get("transaction_id")], Double.parseDouble(c[idx.get("amount")]),
                    (int) Double.parseDouble(c[idx.get("hour")]), c[idx.get("country")], c[idx.get("merchant_category")],
                    (int) Double.parseDouble(c[idx.get("transactions_last_24h")]),
                    Double.parseDouble(c[idx.get("average_amount_last_7d")]), (int) Double.parseDouble(c[idx.get("is_fraud")])));
            } catch (NumberFormatException incomplete) {
                // ligne incomplète (valeur manquante) : ignorée
            }
        }
        return out;
    }
}
