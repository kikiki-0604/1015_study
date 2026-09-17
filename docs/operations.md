# 献立メーカー 実行手順書

2026-09-17 作成 · @Someone

## 事前準備

以下がPC上にインストール・確認済みであることを確認してください。フロント・バックエンドの実行環境自体はコンテナの中に入っているため、Node.jsやJDKを個別にインストールする必要はありません。

- [ ] Docker Desktopがインストールされ、起動している(タスクバーにクジラのアイコンが表示されている状態)
- [ ] Git が使える(`git --version`でバージョンが表示される)
- [ ] ターミナル(Mac: ターミナル.app、Windows: PowerShellやWSL)が使える
- [ ] 配布されたAWSの認証情報(アクセスキー、モデルID、リージョン)を受け取っている

**動作確認コマンド**

```bash
docker --version
docker compose version
```

どちらもバージョンが表示されればOKです。エラーになる場合はDocker Desktopが起動していません。

## 初回セットアップ

### 1. リポジトリを取得する

```bash
git clone <リポジトリのURL>
cd menu-maker
```

### 2. 環境変数ファイルを作成する

`backend/.env.example`をコピーして`backend/.env`を作り、配布されたAWSの値を書き込みます。

```bash
cp backend/.env.example backend/.env
```

`backend/.env`をテキストエディタで開き、以下を埋めます。

```
AWS_ACCESS_KEY_ID=配布された値
AWS_SECRET_ACCESS_KEY=配布された値
AWS_REGION=配布された値
BEDROCK_MODEL_ID=配布された値
```

**注意**: `.env`は絶対にGitHubにpushしないでください(`.gitignore`で除外済みのはずですが、`git status`で`.env`が候補に出ていないか一度確認すると安心です)。

## 起動手順

リポジトリのルート(`docker-compose.yml`がある場所)で、以下を順番に実行します。

### 1. イメージをビルドする(初回・依存関係を変更した時のみ)

```bash
docker compose build
```

フロント・バックエンドのイメージを作る処理です。初回は数分かかります。

### 2. コンテナを起動する

```bash
docker compose up
```

ターミナルにログが流れ続ける状態になります(このターミナルは開いたままにします)。バックグラウンドで動かして別の作業をしたい場合は`-d`を付けます。

```bash
docker compose up -d
```

### 3. 全部のコンテナが起動しているか確認する

```bash
docker compose ps
```

`mysql`・`backend`・`frontend`の3つが`running`(または`healthy`)になっていればOKです。MySQLは起動が少し遅いため、backendが先にエラーを出しても数十秒待てば自動的に接続し直します。

## 動作確認方法

| 確認対象 | 方法 | 期待する結果 |
| --- | --- | --- |
| フロントエンド | ブラウザで `http://localhost:5173` を開く | 食材入力画面が表示される |
| バックエンド | ターミナルで `curl http://localhost:8080/api/records` | `[]`(まだ記録がなければ空配列)が返る。エラーなら backend のログを確認 |
| MySQL | `docker compose exec mysql mysql -uroot -ppassword -e "SHOW TABLES;" menu_maker` | `cooking_records`テーブルが表示される |

フロントエンドから実際に食材を入力して送信し、候補が5件表示されればBedrock連携まで含めて正常に動いています。

## 日常の操作

### ログを見る

```bash
docker compose logs -f backend   # backendだけ
docker compose logs -f           # 全部
```

`-f`を付けると流れ続けます。止めるには`Ctrl + C`。

### コードを直したら

フロント・バックエンドともにコンテナ内へフォルダをマウントしているので、保存すれば自動的に反映されます(ホットリロード)。反映されない場合だけ、以下でそのコンテナを再起動します。

```bash
docker compose restart frontend
docker compose restart backend
```

### 依存パッケージを追加したら

`package.json`(フロント)や`build.gradle.kts`(バックエンド)を変更した場合は再ビルドが必要です。

```bash
docker compose up --build
```

### 停止する

```bash
docker compose down
```

DBのデータも消して完全に初期化したい場合は`-v`を付けます(記録が全部消えるので注意)。

```bash
docker compose down -v
```

## トラブルシューティング

| 症状 | 原因 | 対処 |
| --- | --- | --- |
| `port is already allocated` | 5173・8080・3306番ポートを他のアプリが使っている | 使っているアプリを終了する、または`docker-compose.yml`のポート番号を変更する |
| `Cannot connect to the Docker daemon` | Docker Desktopが起動していない | Docker Desktopを起動してから再実行する |
| backendが起動直後に落ちる/MySQLに繋がらない | MySQLの起動がbackendより遅い | 数十秒待って`docker compose ps`を再確認する。それでも直らなければ`docker compose restart backend` |
| フロントの画面が真っ白 | ビルド失敗、または依存パッケージの不整合 | `docker compose logs -f frontend`でエラー内容を確認し、`docker compose up --build`で再ビルド |
| Bedrock呼び出しで502エラー | `.env`のAWS認証情報・モデルID・リージョンの誤り | `backend/.env`の値を配布された値と再度見比べる |
| 変更したコードが反映されない | マウント設定やキャッシュの問題 | 対象コンテナだけ`docker compose restart <サービス名>`、直らなければ`docker compose down`→`up --build` |

上記で解決しない場合は、`docker compose logs`の内容をそのままチームで共有してください。

## コマンドチートシート

| コマンド | 説明 |
| --- | --- |
| `docker compose build` | イメージをビルドする(初回・依存変更時) |
| `docker compose up` | 起動する(ログ表示あり) |
| `docker compose up -d` | 起動する(バックグラウンド) |
| `docker compose up --build` | 再ビルドしてから起動する |
| `docker compose ps` | 各コンテナの状態を見る |
| `docker compose logs -f [サービス名]` | ログを見る(省略時は全部) |
| `docker compose restart [サービス名]` | 指定コンテナを再起動する |
| `docker compose down` | 停止する(データは残る) |
| `docker compose down -v` | 停止し、DBのデータも削除する |
