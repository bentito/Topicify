/*
Licensed by AT&T under 'Software Development Kit Tools Agreement' 2012.
TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION: http://developer.att.com/sdk_agreement/
Copyright 2012 AT&T Intellectual Property. All rights reserved. 
For more information contact developer.support@att.com http://developer.att.com
*/
package edu.dartmouth.topicify;

/** Configuration parameters for this application's account on Speech API. **/
public class SpeechConfig {
    private SpeechConfig() {} // can't instantiate
    
    /** The URL of AT&T Speech API. **/
    static String serviceUrl() {
        return "https://api.att.com/speech/v3/speechToText";
    }
        
    /** The URL of AT&T Speech API OAuth service. **/
    static String oauthUrl() {
        return "https://api.att.com/oauth/token";
    }
    
    /** The OAuth scope of AT&T Speech API. **/
    static String oauthScope() {
        return "SPEECH";
    }
    
    /** Unobfuscates the OAuth client_id credential for the application. **/
    static String oauthKey() {
        // TODO: Replace this with code to unobfuscate your OAuth client_id.
//        return myUnobfuscate(MY_OBFUSCATED_CLIENT_ID);

        return "e1sb3dpng2kwybp9vbhph5g9ndqkpu6e";
    }

    /** Unobfuscates the OAuth client_secret credential for the application. **/
    static String oauthSecret() {
        // TODO: Replace this with code to unobfuscate your OAuth client_secret.
//        return myUnobfuscate(MY_OBFUSCATED_CLIENT_SECRET);
        return "x6zb8bnavz45tp8pl9rt27rm7hkh9xcg";
    }
}
//curl https://api.att.com/oauth/v4/token --request POST --insecure --data "client_id=e1sb3dpng2kwybp9vbhph5g9ndqkpu6e&client_secret=x6zb8bnavz45tp8pl9rt27rm7hkh9xcg&grant_type=client_credentials&scope=SPEECH,STTC"
//{"access_token":"J8cE8rBFOWs8WWDiORXpjBn84l9AKrA4","token_type":"bearer","expires_in":172800,"refresh_token":"kvpCF5WpuQ2EpIu0cSJ8t1GPk0ADtSMp"}