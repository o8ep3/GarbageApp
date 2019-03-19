import urllib.request
import json
import datetime

class BaseSpeech:
    """シンプルな、発話するレスポンスのベース"""
 
    def __init__(self, speech_text, should_end_session, session_attributes=None):

        """初期化処理
 
        引数:
            speech_text: Alexaに喋らせたいテキスト
            should_end_session: このやり取りでスキルを終了させる場合はTrue, 続けるならFalse
            session_attributes: 引き継ぎたいデータが入った辞書
        """
        if session_attributes is None:
            session_attributes = {}
 
        # 最終的に返却するレスポンス内容。これを各メソッドで上書き・修正していく
        self._response = {
            'version': '1.0',
            'sessionAttributes': session_attributes,
            'response': {
                'outputSpeech': {
                    'type': 'PlainText',
                    'text': speech_text
                },
                'shouldEndSession': should_end_session,
            },
        }
 
        # 取り出しやすいよう、インスタンスの属性に
        self.speech_text = speech_text
        self.should_end_session = should_end_session
        self.session_attributes = session_attributes
 
    def simple_card(self, title, text=None):
        """シンプルなカードを追加する"""
        if text is None:
            text = self.speech_text
        card = {
            'type': 'Simple',
            'title': title,
            'content': text,
        }
        self._response['response']['card'] = card
        return self
 
    def build(self):
        """最後にこのメソッドを呼んでください..."""
        return self._response
 
 
class OneSpeech(BaseSpeech):
    """1度だけ発話する(ユーザーの返事は待たず、スキル終了)"""
 
    def __init__(self, speech_text, session_attributes=None):
        super().__init__(speech_text, True, session_attributes)

#一度の発話とリンクアカウント催促のカードを表示する        
class OneSpeechWithLinkAccount(BaseSpeech):
    
    def __init__(self, speech_text, session_attributes=None):
        super().__init__(speech_text, True, session_attributes)
        
    def account_link_card(self):
        card = {
            'type': 'LinkAccount'
        }
        self._response['response']['card'] = card
        return self
        
class QuestionSpeech(BaseSpeech):
    """発話し、ユーザーの返事を待つ"""
 
    def __init__(self, speech_text, session_attributes=None):
        super().__init__(speech_text, False, session_attributes)
 
    def reprompt(self, text):
        """リプロンプトを追加する"""
        reprompt = {
            'outputSpeech': {
                'type': 'PlainText',
                'text': text
            }
        }
        self._response['response']['reprompt'] = reprompt
        return self
 
#起動時のメッセージ
def welcome():
    message = 'ゴミ捨ての日へようこそ。このスキルではゴミの収集日を確認することができます。「アレクサ、ゴミ捨ての日で明日のゴミを教えて」や「アレクサ、ゴミ捨ての日で次の家庭ごみの日を教えて」などと訪ねてください。'
   # return OneSpeechWithLinkAccount(message).account_link_card().build()
    return OneSpeech(message).build()
 
 #終了時のメッセージ
def finish():
    """終 了"""
    return OneSpeech('終了します。').build()

#アカウントリンクができていない場合はカードを送信
def AccountLinkError():
    message = 'ゴミ捨ての日へようこそ。このスキルではゴミの収集日を確認することができます。「アレクサ、ゴミ捨ての日で明日のゴミを教えて」や「アレクサ、ゴミ捨ての日で次の家庭ごみの日を教えて」などと訪ねてください。このスキルはご利用前に地域設定が必要です。Alexa アプリにアカウントリンクのカードを送りましたので、Alexa アプリでカードを開いて案内に従って地域設定を行ってください。'
    return OneSpeechWithLinkAccount(message).account_link_card().build()

def weekday_conv(num):
    #曜日判定
    if num == 0:
        today_weekday = '月曜'
    elif num == 1:
        today_weekday = '火曜'
    elif num == 2:
        today_weekday = '水曜'
    elif num == 3:
        today_weekday = '木曜'
    elif num == 4:
        today_weekday = '金曜'
    elif num == 5:
        today_weekday = '土曜'
    elif num == 6:
        today_weekday = '日曜'
    return today_weekday

#次の可燃ごみ何？などの質問に対応
def getNextGarbageDay(garbage_day, garbage_type):
    now = datetime.datetime.today()
    day_couner = 0
    while True:
        #「火曜・木曜」みたいな表記
        if garbage_type == '家庭':
            if weekday_conv(now.weekday()) in garbage_day['家庭'].split('・')[0]:
                if garbage_type == '家庭':
                    garbage_type = '家庭ごみ'
                break
        #「1・3水曜」みたいな表記
        elif garbage_type == '紙類':
            if weekday_conv(now.weekday()) == garbage_day['紙類'][3:] and (-(-int(now.day) // 7) == int(garbage_day['紙類'][0]) or -(-int(now.day) // 7) == int(garbage_day['紙類'][2])):
                break
        #家庭、紙以外のゴミの日
        elif weekday_conv(now.weekday()) == garbage_day['プラ']:
            if garbage_type == 'プラ':
                garbage_type = 'プラスチック'
            break
        elif weekday_conv(now.weekday()) == garbage_day['缶・びん']:
            break
            
        day_couner += 1
        now += datetime.timedelta(days=1)
    if day_couner == 0:
        message = '{}の日は今日です。'.format(garbage_type)
    else:
        message = '{}の日は{}日後です。'.format(garbage_type, day_couner)
            
    return OneSpeech(message).build()

#明日のゴミは何？に対応
def getTmrGarbageDay(garbage_day, day):
    now = datetime.datetime.today()
    if day == '明日':
        now += datetime.timedelta(days=1)
    elif day == '明後日':
        now += datetime.timedelta(days=2)
    elif day == '今日':
        pass
    else:
        return OneSpeech('すみません。今日、明日、明後日にのみ対応しています。').build()
    #曜日判定
    today_weekday = weekday_conv(now.weekday())

    #ゴミの日判定
    today_garbage = []
    for garbage, weekday in garbage_day.items():
        #「火曜・木曜」みたいな表記
        if garbage == '家庭':
            if weekday.split('・')[0] == today_weekday:
                today_garbage.append(garbage + 'ゴミ')
            elif weekday.split('・')[1] == today_weekday:
                today_garbage.append(garbage + 'ゴミ')
        #「1・3水曜」みたいな表記
        elif garbage == '紙類':
            #曜日が等しく、日にち/7の切り上げが1 or 3と等しい場合
            if weekday[3:] == today_weekday and (-(-int(now.day) // 7) == int(weekday[0]) or -(-int(now.day) // 7) == int(weekday[2])):
                today_garbage.append(garbage)
        #それ以外のゴミで曜日が等しい場合
        elif garbage == 'プラ' and weekday == today_weekday:
            today_garbage.append('プラスチックゴミ')
        elif weekday == today_weekday:
            today_garbage.append(garbage)
    #print(today_garbage)
    if len(today_garbage) == 1:
        message = day + 'は' + today_garbage[0] + 'の日です。'
    elif len(today_garbage) == 2:
        message = day +  'は' + today_garbage[0] + 'と' + today_garbage[1] + 'の日です。'
    else:
        message = day + 'はゴミの日ではありません。' 
    return OneSpeech(message).build()
    
    
 
def lambda_handler(event, context):
    """最初に呼び出される関数"""
    # リクエストの種類を取得
    request = event['request']
    request_type = request['type']
    
    #住所を取得
    try:
        address = event['context']['System']['user']['accessToken'].split(':')
    #アカウントリンクがまだの場合
    except KeyError:
        return AccountLinkError()
    #ゴミ捨ての日を取得
    garbage_day = getGarbageInfo(address)

    #起動時
    if request_type == 'LaunchRequest':
        return welcome()
 
    # 何らかのインテントだった場合
    elif request_type == 'IntentRequest':
        intent_name = request['intent']['name']
 
        # 次のゴミの日を教えてに対応
        if intent_name == 'getNextGarbageDayIntent':
            match_check = request['intent']['slots']['garbage_type']['resolutions']['resolutionsPerAuthority'][0]['status']['code']
            if match_check == 'ER_SUCCESS_MATCH':
                garbage_type = request['intent']['slots']['garbage_type']['resolutions']['resolutionsPerAuthority'][0]['values'][0]['value']['name']
                return getNextGarbageDay(garbage_day, garbage_type)
            else:
                return OneSpeech('すみません。分かりませんでした。').build()
        
        #今日or明日or明後日のゴミの日教えてに対応
        elif intent_name == 'getTmrGarbageDayIntent':
            match_check = request['intent']['slots']['day']['resolutions']['resolutionsPerAuthority'][0]['status']['code']
            if match_check == 'ER_SUCCESS_MATCH':
                day = request['intent']['slots']['day']['resolutions']['resolutionsPerAuthority'][0]['values'][0]['value']['name']
                return getTmrGarbageDay(garbage_day, day)
            else:
                return OneSpeech('すみません。今日、明日、明後日にのみ対応しています。').build()
 
        # 「ヘルプ」「どうすればいいの」「使い方を教えて」で呼ばれる、組み込みインテント
        elif intent_name == 'AMAZON.HelpIntent':
            return welcome()
 
        # 「キャンセル」「取り消し」「やっぱりやめる」等で呼び出される。組み込みのインテント
        elif intent_name == 'AMAZON.CancelIntent' or intent_name == 'AMAZON.StopIntent':
            return finish()
        