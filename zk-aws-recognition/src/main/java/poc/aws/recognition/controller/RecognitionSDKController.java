package poc.aws.recognition.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import poc.aws.recognition.CredentialConfig;
import poc.aws.recognition.CustomAwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class RecognitionSDKController {


    private static final Logger log = LogManager.getLogger(RecognitionSDKController.class);
    private List<Integer> num = Arrays.asList(1, 2, 3, 4, 5);
    @GetMapping(value = "/recognition/session/sdk/get")
    public String createSession()
    {

        Region region = Region.US_EAST_1;
        RekognitionClient rekClient = RekognitionClient.builder()
                .credentialsProvider(new CustomAwsCredentialsProvider(CredentialConfig.AccessKey, CredentialConfig.SecretKey))
                .region(region)
                .build();

        String sessionId = createFaceLivenessSession(rekClient);
        String sessionResults = getFaceLivenessSessionResults(rekClient, sessionId);

        rekClient.close();

//        String sessionResult = getAwsRekognitionResult(sessionId); //getAwsRekognitionSessionResult(sessionId);

        return  sessionResults.toString();
    }

    private String createFaceLivenessSession(RekognitionClient rekClient) {
        try {

            CreateFaceLivenessSessionRequest livenessSessionRequest = CreateFaceLivenessSessionRequest
                    .builder()
                    .build();

            CreateFaceLivenessSessionResponse livenessSessionResponse = rekClient.createFaceLivenessSession(livenessSessionRequest);
            System.out.println("There is a session id : " + livenessSessionResponse.sessionId());
            return livenessSessionResponse.sessionId();
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
            return null;
        }
    }

    private String getFaceLivenessSessionResults(RekognitionClient rekClient, String sessionId) {
        try {
            GetFaceLivenessSessionResultsRequest livenessSessionRequest = GetFaceLivenessSessionResultsRequest
                    .builder()
                    .sessionId(sessionId)
                    .build();

            GetFaceLivenessSessionResultsResponse livenessSessionResponse = rekClient.getFaceLivenessSessionResults(livenessSessionRequest);
            System.out.println("There is a session id : " + livenessSessionResponse.sessionId());
            return livenessSessionResponse.toString();
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
            return null;
        }
    }

}
