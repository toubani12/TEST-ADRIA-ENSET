package toubani.badr.transactionservice.web.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import toubani.badr.transactionservice.exception.AccountNotFoundException;
import toubani.badr.transactionservice.exception.AccountServiceUnavailableException;
import toubani.badr.transactionservice.exception.BusinessRuleException;
import toubani.badr.transactionservice.exception.TransactionNotFoundException;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        ErrorType type;
        if (ex instanceof AccountNotFoundException || ex instanceof TransactionNotFoundException) {
            type = ErrorType.NOT_FOUND;
        } else if (ex instanceof BusinessRuleException) {
            type = ErrorType.BAD_REQUEST;
        } else if (ex instanceof AccountServiceUnavailableException) {
            type = ErrorType.INTERNAL_ERROR;
        } else {
            return null;
        }
        return GraphqlErrorBuilder.newError(env)
                .errorType(type)
                .message(ex.getMessage())
                .build();
    }
}
