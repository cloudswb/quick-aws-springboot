package com.aws.samples.opensearch.config;

/*
 *        Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *        Permission is hereby granted, free of charge, to any person obtaining a copy of this
 *        software and associated documentation files (the "Software"), to deal in the Software
 *        without restriction, including without limitation the rights to use, copy, modify,
 *        merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *        permit persons to whom the Software is furnished to do so.
 *
 *        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *        INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 *        PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *        HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *        OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *        SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * @author Angel Conde
 *
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.aws.samples.opensearch.repository")
@Slf4j
public class OpenSearchRestClientConfiguration extends AbstractElasticsearchConfiguration {

    @Value("${aws.os.endpoint}")
    private String endpoint = "";

    @Value("${aws.os.region}")
    private String region = "";

    @Value("${aws.os.username}")
    private String username = "";


    @Value("${aws.os.password}")
    private String password = "";

    private AWSCredentialsProvider credentialsProvider = null;

    @Autowired
    public OpenSearchRestClientConfiguration(AWSCredentialsProvider provider){
        credentialsProvider=provider;
    }

    /**
     * SpringDataElasticSearch data provides us the flexibility to implement our custom {@link RestHighLevelClient} instance by implementing the abstract method {@link AbstractElasticsearchConfiguration#elasticsearchClient()},
     *
     * @return RestHighLevelClient. AWS ElasticService Https rest calls have to be signed with AWS credentials, hence an interceptor {@link HttpRequestInterceptor} is required to sign every
     * API calls with credentials. The signing is happening through the below snippet
     * <code>
     * signer.sign(signableRequest, awsCredentialsProvider.getCredentials());
     * </code>
     */
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        AWS4Signer signer = new AWS4Signer();
        String serviceName = "es";
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        // HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(HttpHost.create(endpoint))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        // RestClientBuilder restClientBuilder = RestClient.builder(HttpHost.create(endpoint))
        //        .setHttpClientConfigCallback(e -> e.addInterceptorLast(interceptor));

        return new RestHighLevelClient(builder);
    }

}
