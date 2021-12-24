# 概要
以下のブログのサンプルコード。

https://blog.takehata-engineer.com/entry/deploy-kotlin-applications-to-aws-lambda-using-kotless

# 起動方法
## 事前準備
### AWSの環境整備、設定
以下の3つの準備が必要になります

- AWSアカウントの作成
- Amazon S3のバケット作成
- AWS CLIのCredentials取得

AWSアカウント作成後、以下の記事の`S3のバケットを作成`、`AWS CLIのクレデンシャルを取得`の手順を参考にローカル環境に設定してください。

https://blog.takehata-engineer.com/entry/deploy-kotlin-applications-to-aws-lambda-using-kotless

S3のバケット名は、本アプリケーションでは`example-kotless-and-dynamodb`としています。

### Twitter APIの登録とAPIキー、トークンの取得
Twitter APIに登録し、実行に必要な各種情報を取得します。  
以下のサイトなどを参考に、ご自分のTwitterアカウントで登録してください。

https://www.itti.jp/web-direction/how-to-apply-for-twitter-api/

### Twitterの認証情報の設定ファイルを作成
`src/main/resources`配下に、`twitter.yaml`という名前のファイルを作成し、以下のような内容を記述します。

```yaml
consume_key: XXXXXXXXXXXXXXXXXXXXX
consume_secret: XXXXXXXXXXXXXXXXXXXXX
access_token: XXXXXXXXXXXXXXXXXXXXX
access_token_secret: XXXXXXXXXXXXXXXXXXXXX
account_name: hogehoge 
```

`consume_key`〜`access_token_secret`は、前述のTwitter APIの登録後に取得した情報を記述してください。  
`account_name`には、取得したいご自身のTwitterのアカウント名を設定します。

## アプリケーションの実行
ローカル環境での実行はGradleの`local`、AWS環境へは`plan`、`deploy`というタスクを使用してデプロイします。  
詳しくは以下の記事の`planを実行`、`deployを実行`のセクションを御覧ください。

https://blog.takehata-engineer.com/entry/deploy-kotlin-applications-to-aws-lambda-using-kotless

# アプリケーションについて
ブログ記事の中でも書いていますが、DynamoDB.ktの`putTweetList`がツイートを収集してDynamoDBへ登録する処理、`getTweetListByMonthDay`がDynamoDBからツイートの情報を取得する処理になります。

Routing.ktにある`registerTweet`は、記事内にはありませんが定期実行の登録処理をデバッグするために用意しているAPIです。  
関数内で渡している引数の値を任意の日時に変えることで、その期間のデータ登録を実行します。  
(1週間より前の日時を指定してもTwitter APIで取得できないため登録されません)