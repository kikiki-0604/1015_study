# 献立メーカー

冷蔵庫にある食材を入力すると、AI(AWS Bedrock)が作れる料理の候補を5件提案するWebアプリです。
候補を選ぶと外部レシピサイトに遷移でき、実際に作った料理は日付付きで記録・再閲覧できます。

## 技術スタック

- フロントエンド: React + TypeScript(Vite)
- バックエンド: Kotlin + Spring Boot
- DB: MySQL 8.0
- AI連携: AWS Bedrock(Claude)

詳細は `docs/` フォルダの設計書を参照してください。

- [要件定義書](docs/requirements.md)
- [基本設計書](docs/basic-design.md)
- [詳細設計書](docs/detailed-design.md)
- [実行手順書](docs/operations.md)

## クイックスタート

```bash
# 1. 環境変数ファイルを作成し、AWSの値を入れる
cp backend/.env.example backend/.env

# 2. ビルドして起動する
docker compose up --build
```

- フロントエンド: http://localhost:5173
- バックエンド: http://localhost:8080

詳しい手順・トラブルシューティングは [実行手順書](docs/operations.md) を参照してください。

## 補足(実装時の判断)

- `docker-compose.yml` はソースコードのボリュームマウントを行わず、ビルド済みイメージを起動する構成にしています(このリポジトリはこの後バグ入りバージョンの土台になるため、まずは安定して再現よく起動することを優先しました)。開発中にホットリロードを使いたい場合は、基本設計書の構成例を参考にボリュームマウントを追加してください。
- レシピの遷移先は、AIが提案した料理名をもとにCookpadのサイト内検索URLを組み立てる方式にしています(特定レシピへの直接リンクは公式APIがないため対応していません)。
