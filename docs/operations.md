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

`backend/.env`をテキストエディタで開き、以下を埋めます。Bedrock APIキー(Claude Code拡張と共通のキー)を持っている場合は方式Aだけで十分です。

```
# 方式A(推奨): Bedrock APIキーを使う
AWS_BEARER_TOKEN_BEDROCK=配布された値
AWS_REGION=配布された値
BEDROCK_MODEL_ID=配布された値

# 方式B: 従来のアクセスキーを使う場合のみ埋める(方式Aを使うなら空欄でOK)
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
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
| バックエンド | ターミナルで `curl http://localhost:8080/api/records` | 初期投入されたサンプル記録(肉じゃが、オムライスなど6件)がJSON配列で返る。エラーなら backend のログを確認 |
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

このリポジトリの`docker-compose.yml`は、ソースコードのボリュームマウント(ホットリロード)を行わない構成になっています(バグ入りバージョンの土台として、まず安定して再現よく起動することを優先したため)。そのため**コードを保存しただけでは反映されません**。変更を反映するには、対象サービスを再ビルドしてください。

```bash
docker compose up --build backend    # backendだけ再ビルド
docker compose up --build frontend   # frontendだけ再ビルド
docker compose up --build            # まとめて再ビルド
```

`docker compose restart <サービス名>`は再ビルドを行わないため、コード変更には効きません(設定ファイルの再読み込みなど、イメージを作り直さない場合のみ有効です)。開発中にホットリロードを使いたい場合は、`docker-compose.yml`の該当サービスに`volumes`でソースディレクトリをマウントする設定を追加し、`command`をdevサーバー起動コマンドに変更してください(frontendは`npm run dev -- --host 0.0.0.0`、backendは`gradle bootRun`相当)。

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
| 変更したコードが反映されない | このリポジトリはソースをマウントしていないため、保存しただけでは反映されない | 対象サービスを`docker compose up --build <サービス名>`で再ビルドする |
| `docker compose exec mysql mysql ...`で日本語が`?`や文字化けで表示される | データではなく`mysql`CLIクライアント自身の表示文字コードがデフォルトで`latin1`になっているだけの場合が多い | `docker compose exec mysql mysql --default-character-set=utf8mb4 -uroot -ppassword -e "SELECT ..." menu_maker`のように`--default-character-set=utf8mb4`を付けて再確認する。それでも化けている場合はDBの実データが壊れている(下記参照) |
| 履歴画面の料理名が本当に文字化けして保存されている | `init.sql`に`SET NAMES utf8mb4;`が無い状態でDBを初期作成すると、UTF-8の日本語が二重エンコードされて実データとして壊れる(初回起動時のみ発生。既に修正済みなら発生しない) | `backend/db/init.sql`の先頭に`SET NAMES utf8mb4;`があることを確認した上で、`docker compose down -v`→`docker compose up -d`でDBボリュームを作り直す(記録は全て消えて再シードされる) |

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
