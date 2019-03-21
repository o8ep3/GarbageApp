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
    
    //ロード時は隠す
    $('[name=garbage_specific]').hide();
    // 現在のユーザーの属性情報，ごみ収集日を取得・表示
    getUserAttribute();

    //セレクトボックスの処理
    $('[name=garbage_detail]').change(function(){
        var val = $(this).val();
        if (val == '特定の曜日') {
            $('[name=garbage_specific]').show();
            $('[name=garbage_day]').hide();
        } else if (val == '毎週') {
            $('[name=garbage_specific]').hide();
            $('[name=garbage_day]').show();
        }
    });

    //テーブルの追加
    $('#add').click(function(){
        var garbage_type = $('[name=garbage_type]').val();
        var garbage_detail = $('[name=garbage_detail]').val();
        if (garbage_detail == '特定の曜日') {
            garbage_day = $('[name=garbage_specific]').val();
        } else if (garbage_detail == '毎週') {
            garbage_day = $('[name=garbage_day]').val();
        }
        $('#garbage_schedule').append('<tr><td>' + garbage_type + '</td><td>' + garbage_detail + '</td><td>' + garbage_day + '</td><td><input type="button" id="delete" value="削除"></td></tr>');
    });

    //テーブルの削除
    $('#garbage_schedule').on('click', '#delete', function(){
        var row = $(this).closest("tr").remove();
        $(row).remove();
    });

    //ログアウト処理をしたらログインページに移動
    $('#logout').click(function(){
        cognitoUser.signOut();
        alert('ログアウトしました．')
        $(location).attr("href", "signin.html");
    });

    //保存が押されたらテーブルにある収集日情報をdynamoDBにpostする
    $('#save').click(function(){
        postGarbageDaySchedule();
    });
});
 

/**
 * 現在のユーザーの属性情報を取得・表示する
 */
var getUserAttribute = function(){
	
    // 現在のユーザー情報が取得できているか？
    if (cognitoUser != null) {
        cognitoUser.getSession(function(err, session) {
            if (err) {
                //console.log(err);
                $(location).attr("href", "signin.html");
            } else {
                // ユーザの属性を取得
                cognitoUser.getUserAttributes(function(err, result) {
                    if (err) {
                        $(location).attr("href", "signin.html");
                    }
                    //トークンを取得
                    idToken = session.getIdToken().getJwtToken(); 
                    // 取得した属性情報を連想配列に格納
                    for (i = 0; i < result.length; i++) {
                        currentUserData[result[i].getName()] = result[i].getValue();
                    }
                    console.log(currentUserData);
                    $("div#menu p").text("ようこそ！" + currentUserData["email"] + "さん");
                    getGarbageSchedule();
                });
            }
        });
    } else {
        $(location).attr("href", "signin.html");
    }
};

//IdTokenが読み込まれた時，ユーザに紐付いたごみ収集日をdynamoDBから取得
var getGarbageSchedule = function(){
    console.log(idToken);
    $.ajax(
        "https://hkhkpqbf4i.execute-api.ap-northeast-1.amazonaws.com/prod",
        {
            type: 'GET',
            contentType: 'application/json',
            headers: {
                Authorization: idToken
            },
            async: false,
            cache: false
        }
    )
    .done(function(data) {
        //console.log(jQuery.parseJSON(data));
        //ごみ収集日が登録されていない場合
        if (data == 'KeyError') {
            console.log('KeyError');
        } else {
            //登録されている場合はテーブルに出力
            for (var item in data) {
                if (item != 'email') {
                    garbage_info = JSON.parse(data[item]);
                    var garbage_type = garbage_info['garbage_type'];
                    var garbage_detail = garbage_info['garbage_timing'];
                    var garbage_day = garbage_info['garbage_day'];
                    $('#garbage_schedule').append('<tr><td>' + garbage_type + '</td><td>' + garbage_detail + '</td><td>' + garbage_day + '</td><td><input type="button" id="delete" value="削除"></td></tr>');
                }
            }
        }
    })
    .fail(function() {
        console.log("failed to call api");
    });
};

//保存が押されたらテーブルにある収集日情報をdynamoDBにpostする
var postGarbageDaySchedule = function() {
    var dic = {"email" : currentUserData["email"]};
    var arr = [];
    console.log(dic)
    //テーブルの値をObject配列に格納する
    $("tr").each(function(i){
        $(this).children().each(function(j){
            arr[j] = $(this).text();
        })
        if (i >= 1) {
            dic[String(i - 1)] = '{"garbage_type :' +  arr[0] + ', "garbage_timing" : ' + arr[2] + ', "garbage_day" : ' + arr[3] + '}';
        }
    })
    //api gateway postで書き込み
    console.log(dic)
    //console.log(arr);
};