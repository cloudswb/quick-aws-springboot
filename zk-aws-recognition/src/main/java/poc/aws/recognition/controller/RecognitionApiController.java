package poc.aws.recognition.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import poc.aws.recognition.AwsRekognitionHeaderUtil;

import java.util.Arrays;
import java.util.List;

@RestController
public class RecognitionApiController {


    private static final Logger log = LogManager.getLogger(RecognitionApiController.class);
    private List<Integer> num = Arrays.asList(1, 2, 3, 4, 5);

    public static final String END_POINTURL = "https://rekognition.us-west-2.amazonaws.com";

    public static final String CREATE_FACELIVENESS_SESSION = "RekognitionService.CreateFaceLivenessSession";
    public static final String GET_FACELIVENESS_SESSION_RESULTS = "RekognitionService.GetFaceLivenessSessionResults";
    public static final String COMPARE_FACES = "RekognitionService.CompareFaces";


    /**
     * Create a sample log output.
     * @return
     */
    @GetMapping(value = "/recognition/session/api/get")
    public String createOneLog()
    {
        String sessionId = getAwsRekognitionSessionId();
        String sessionResult = getAwsRekognitionResult(sessionId); //getAwsRekognitionSessionResult(sessionId);

        return  sessionResult;
    }

    public String getAwsRekognitionResult(String sessionIdDto) {
        // 断言sessionId不能为空
//        Assert.notNull(sessionId, "sessionId不能为空");
//        String sessionIdDto = JSON.toJSONString(new FaceLivenessSessionResultsDto(sessionId));
        HttpHeaders httpHeaders = AwsRekognitionHeaderUtil.buildGetSessionIdHeaders(GET_FACELIVENESS_SESSION_RESULTS, sessionIdDto);
        HttpEntity<String> httpEntity = new HttpEntity<>(sessionIdDto, httpHeaders);
        byte[] body = null;
        try {
            body = new RestTemplate().exchange(END_POINTURL, HttpMethod.POST, httpEntity, byte[].class).getBody();
        }catch (RestClientException e){
            log.error("getAwsRekognitionResult RestClientException:", e);
        }catch (Exception e){
            log.error("getAwsRekognitionResult error", e);
        }
        log.info("getAwsRekognitionResult body:{}", body);
        return new String(body);
    }

    public String getAwsRekognitionSessionId() {
        HttpHeaders httpHeaders = AwsRekognitionHeaderUtil.buildGetSessionIdHeaders(CREATE_FACELIVENESS_SESSION, "{}");
        byte[] body = new RestTemplate().exchange(END_POINTURL, HttpMethod.POST, new HttpEntity<>("{}", httpHeaders), byte[].class).getBody();
        return new String(body);
    }

    public String getAwsRekognitionSessionResult(String sessionId) {
        HttpHeaders httpHeaders = AwsRekognitionHeaderUtil.buildGetSessionIdHeaders(GET_FACELIVENESS_SESSION_RESULTS, sessionId);
        byte[] body = new RestTemplate().exchange(END_POINTURL, HttpMethod.POST, new HttpEntity<>(sessionId, httpHeaders), byte[].class).getBody();
        return new String(body);
    }

}
