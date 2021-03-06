package com.oembedler.moon.graphql.boot;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.coxautodev.graphql.tools.SchemaParser;
import com.coxautodev.graphql.tools.SchemaParserBuilder;
import com.coxautodev.graphql.tools.SchemaParserDictionary;
import com.coxautodev.graphql.tools.SchemaParserOptions;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.servlet.GraphQLSchemaProvider;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Andrew Potter
 */
@Configuration
@ConditionalOnClass(SchemaParser.class)
public class GraphQLJavaToolsAutoConfiguration {

    @Autowired(required = false)
    private SchemaParserDictionary dictionary;

    @Autowired(required = false)
    private GraphQLScalarType[] scalars;

    @Autowired(required = false)
    private SchemaParserOptions options;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnBean({GraphQLResolver.class})
    @ConditionalOnMissingBean
    public SchemaParser schemaParser(List<GraphQLResolver<?>> resolvers) throws IOException {

        SchemaParserBuilder builder = dictionary != null ? new SchemaParserBuilder(dictionary) : new SchemaParserBuilder();

        Resource[] resources = applicationContext.getResources("classpath*:**/*.graphqls");
        if(resources.length <= 0) {
            throw new IllegalStateException("No *.graphqls files found on classpath.  Please add a graphql schema to the classpath or add a SchemaParser bean to your application context.");
        }

        for(Resource resource : resources) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(resource.getInputStream(), writer);
            builder.schemaString(writer.toString());
        }

        if(scalars != null) {
            builder.scalars(scalars);
        }

        if(options != null) {
            builder.options(options);
        }

        return builder.resolvers(resolvers)
            .build();
    }


    @Bean
    @ConditionalOnBean(SchemaParser.class)
    @ConditionalOnMissingBean({GraphQLSchema.class, GraphQLSchemaProvider.class})
    public GraphQLSchema graphQLSchema(SchemaParser schemaParser) {
        return schemaParser.makeExecutableSchema();
    }
}
