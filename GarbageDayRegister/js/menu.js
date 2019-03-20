// ユーザープールの設定
const poolData = {
    UserPoolId : 'ap-northeast-1_8xcHUOVG5',
    ClientId : '11rosngju1bnn0lv14j0b06dna'
};
const userPool = new AmazonCognitoIdentity.CognitoUserPool(poolData);
const cognitoUser = userPool.getCurrentUser();  // 現在のユーザー
 
var currentUserData = {};  // ユーザーの属性情報
 
/**
 * 画面読み込み時の処理
 */
$(document).ready(function() {
 
    // Amazon Cognito 認証情報プロバイダーの初期化
    AWSCognito.config.region = 'ap-northeast-1'; // リージョン
    AWSCognito.config.credentials = new AWS.CognitoIdentityCredentials({
        IdentityPoolId: 'ap-northeast-1:e404b702-6447-4568-879b-561c3c3cfeab'
    });
    
    console.log(AWSCognito.config.credentials);
		    
    // 現在のユーザーの属性情報を取得・表示
    getUserAttribute();
});
 
/**
 * 現在のユーザーの属性情報を取得・表示する
 */
var getUserAttribute = function(){
	
    // 現在のユーザー情報が取得できているか？
    if (cognitoUser != null) {
        cognitoUser.getSession(function(err, session) {
            if (err) {
                console.log(err);
                $(location).attr("href", "signin.html");
            } else {
                // ユーザの属性を取得
                cognitoUser.getUserAttributes(function(err, result) {
                    if (err) {
                        $(location).attr("href", "signin.html");
                    }
                    
                    // 取得した属性情報を連想配列に格納
                    for (i = 0; i < result.length; i++) {
                        currentUserData[result[i].getName()] = result[i].getValue();
                    }
                    console.log(currentUserData);
                    $("div#menu h1").text("ようこそ！" + currentUserData["email"] + "さん");
                });
            }
        });
    } else {
        $(location).attr("href", "signin.html");
    }
};