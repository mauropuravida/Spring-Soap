package schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;


@Endpoint
public class TestEndpoint {

    private static final String NAMESPACE_URI = "http://www/schema";
    
    private TestRepository testRepository;
    
    @Autowired
    public TestEndpoint(TestRepository testRepository) {
        this.testRepository = testRepository;
    }
 
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getInfoAnimalRequest")
    @ResponsePayload
    public GetInfoAnimalResponse getInfoCow(@RequestPayload GetInfoAnimalRequest request) {
    	System.out.print("get animal");
    	GetInfoAnimalResponse response = new GetInfoAnimalResponse();
        response.setData(testRepository.getInfoCow(request.getId()));
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createCowRequest")
    @ResponsePayload
    public CreateCowResponse addCow(@RequestPayload CreateCowRequest request) {
    	CreateCowResponse response = new CreateCowResponse();
        response.setResult(testRepository.addCow(request));
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addBcsRequest")
    @ResponsePayload
    public AddBcsResponse addBcs(@RequestPayload AddBcsRequest request) {
    	AddBcsResponse response = new AddBcsResponse();
        response.setResult(testRepository.addCowBcs(request));
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "setAnimalAlertRequest")
    @ResponsePayload
    public SetAnimalAlertResponse addAnimalAlert(@RequestPayload SetAnimalAlertRequest request) {
    	SetAnimalAlertResponse response = new SetAnimalAlertResponse();
        response.setResult(testRepository.setAnimalAlert(request));
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "setHerdAlertRequest")
    @ResponsePayload
    public SetHerdAlertResponse addHerdAlert(@RequestPayload SetHerdAlertRequest request) {
    	SetHerdAlertResponse response = new SetHerdAlertResponse();
        response.setResult(testRepository.setHerdAlert(request));
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getInfoRodeoRequest")
    @ResponsePayload
    public GetInfoRodeoResponse infoRode(@RequestPayload GetInfoRodeoRequest request) {
    	GetInfoRodeoResponse response = new GetInfoRodeoResponse();
        response.setInfoHerd(testRepository.getInfoRodeo(request));
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addHerdRequest")
    @ResponsePayload
    public AddHerdResponse infoRode(@RequestPayload AddHerdRequest request) {
    	AddHerdResponse response = new AddHerdResponse();
        response.setResult(testRepository.addHerd(request));
        return response;
    }
}
