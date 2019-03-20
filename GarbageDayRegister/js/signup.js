// ユーザープールの設定
const poolData = {
    UserPoolId : 'ap-northeast-1_8xcHUOVG5',
    ClientId : '11rosngju1bnn0lv14j0b06dna'
};
const userPool = new AmazonCognitoIdentity.CognitoUserPool(poolData);
 
var attributeList = [];
 
/**
 * 画面読み込み時の処理
 */
$(document).ready(function() {
		
	// Amazon Cognito 認証情報プロバイダーの初期化
	AWSCognito.config.region = 'ap-northeast-1'; // リージョン
	AWSCognito.config.credentials = new AWS.CognitoIdentityCredentials({
	    IdentityPoolId: 'ap-northeast-1:e404b702-6447-4568-879b-561c3c3cfeab'
	});
		    
	// 「Create Account」ボタン押下時
	$("#createAccount").click(function(event) {
	    signUp();
	});
});
 
/**
 * サインアップ処理。
 */
var signUp = function() {
			
	var username = $("#email").val();
	var password = $("#password").val();
			
	// 何か1つでも未入力の項目がある場合、処理終了
    if (!username | !password) { 
    	return false; 
    }
		    
    // ユーザ属性リストの生成
    var emailInfo = {
        Name : 'email',
        Value : username
    }
  
	var attributeEmail = new AmazonCognitoIdentity.CognitoUserAttribute(emailInfo);	
    attributeList.push(attributeEmail);
			
    // サインアップ処理
    userPool.signUp(username, password, attributeList, null, function(err, result){
	    if (err) {
	    	alert(err);
			return;
	    } else {
            alert('検証コードを送信しましたのでご確認ください．');
            // サインアップ成功の場合、アクティベーション画面に遷移する
            $(location).attr("href", "activation.html");
	    }
    });
}