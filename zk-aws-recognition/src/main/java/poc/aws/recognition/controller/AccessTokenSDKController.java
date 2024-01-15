package poc.aws.recognition.controller;


import poc.aws.recognition.CredentialConfig;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.model.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
public class AccessTokenSDKController {


    private static final Logger log = LogManager.getLogger(AccessTokenSDKController.class);
    private List<Integer> num = Arrays.asList(1, 2, 3, 4, 5);
    /**
     * Create a sample log output.
     * @return
     */
    @GetMapping(value = "/sts/token/get")
    public String getAccessToken()
    {
        AwsBasicCredentials creds = AwsBasicCredentials.create(CredentialConfig.AccessKey, CredentialConfig.SecretKey);
        // Create StaticCredentialsProvider
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(creds);
        // Build STS client
        StsClient stsClient = StsClient.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(Region.US_EAST_1)
                .build();
        GetSessionTokenRequest tokenRequest = GetSessionTokenRequest.builder()
                .durationSeconds(15 * 60) // 15 minutes
                .build();

        Credentials credentials = stsClient.getSessionToken(tokenRequest).credentials();
        String result = "accessKeyId:" + "   " +
                credentials.accessKeyId() + "   " +
                "secretAccessKey:" + "   " +
                credentials.secretAccessKey() +"  " +
                "sessionToken:" +"   " +
                credentials.sessionToken() +"   " +
                "expiration:" +"   " +
                credentials.expiration();

        stsClient.close();
        return result;
    }

    public static void assumeGivenRole(StsClient stsClient, String roleArn, String roleSessionName) {
        try {
            AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName(roleSessionName)
                    .build();

            AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);
            Credentials myCreds = roleResponse.credentials();

            // Display the time when the temp creds expire.
            Instant exTime = myCreds.expiration();
            String tokenInfo = myCreds.sessionToken();

            // Convert the Instant to readable date.
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .withLocale(Locale.US)
                            .withZone(ZoneId.systemDefault());

            formatter.format(exTime);
            System.out.println("The token " + tokenInfo + "  expires on " + exTime);

        } catch (StsException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
