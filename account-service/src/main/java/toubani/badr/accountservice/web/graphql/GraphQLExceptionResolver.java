package toubani.badr.accountservice.web.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import toubani.badr.accountservice.exception.AccountNotFoundException;
import toubani.badr.accountservice.exception.BusinessRuleException;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        ErrorType type;
        if (ex instanceof AccountNotFoundException) {
            type = ErrorType.NOT_FOUND;
        } else if (ex instanceof BusinessRuleException || ex instanceof IllegalArgumentException) {
            type = ErrorType.BAD_REQUEST;
        } else {
            return null;
        }
        return GraphqlErrorBuilder.newError(env)
                .errorType(type)
                .message(ex.getMessage())
                .build();
    }
}
