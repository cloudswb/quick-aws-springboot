package poc.aws.recognition;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

public class CustomAwsCredentialsProvider implements AwsCredentialsProvider {

    private String accessKeyId;
    private String secretAccessKey;

    public CustomAwsCredentialsProvider(String accessKeyId, String secretAccessKey ){
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;

    }
    @Override
    public AwsCredentials resolveCredentials() {
        return new AwsCredentials() {
            @Override
            public String accessKeyId() {
                return accessKeyId;
            }

            @Override
            public String secretAccessKey() {
                return secretAccessKey;
            }
        };
    }
}
