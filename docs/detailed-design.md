# 献立メーカー 詳細設計書

2026-09-17 作成 · @Someone

## バックエンドモジュール構成

```
com.example.menumaker
├─ controller
│   ├─ SuggestionController.kt   # POST /api/suggestions
│   └─ RecordController.kt       # POST・GET /api/records
├─ service
│   ├─ SuggestionService.kt      # プロンプト生成、Bedrock呼び出し、JSONパース
│   └─ RecordService.kt          # 記録の登録・取得
├─ client
│   └─ BedrockClient.kt          # AWS SDKのラッパー
├─ repository
│   └─ CookingRecordRepository.kt # Spring Data JPA
├─ entity
│   └─ CookingRecord.kt          # cooking_recordsテーブルに対応
├─ dto
│   ├─ SuggestionRequest.kt / SuggestionResponse.kt / Candidate.kt
│   └─ RecordRequest.kt / RecordResponse.kt
└─ exception
    └─ GlobalExceptionHandler.kt  # @ControllerAdviceで一元処理
```

| クラス | 責務 |
| --- | --- |
| SuggestionController | リクエストの受付とレスポンスの返却のみ。ロジックは持たない |
| SuggestionService | プロンプト組み立て、BedrockClient呼び出し、JSON配列のパース |
| BedrockClient | AWS SDKでの`InvokeModel`呼び出しをラップし、他クラスからAWS依存を隠す |
| RecordController | リクエストの受付とレスポンスの返却のみ |
| RecordService | バリデーション後、Repositoryを呼び出す |
| CookingRecordRepository | `cooking_records`テーブルへのCRUD(JPA標準メソッドのみで十分) |
| GlobalExceptionHandler | 各Serviceが投げた例外をHTTPレスポンスに変換 |

## フロントエンドコンポーネント構成

```
src/
├─ pages/
│   ├─ IngredientInputPage.tsx   # 食材入力画面
│   ├─ CandidateListPage.tsx     # 候補一覧画面(選択のみ)
│   ├─ CandidateDetailPage.tsx   # 候補詳細画面(栄養情報+作るボタン)
│   └─ HistoryPage.tsx           # 履歴画面
├─ components/
│   ├─ IngredientTagInput.tsx    # 食材タグの入力・削除
│   ├─ CandidateCard.tsx         # 候補1件分のカード表示
│   └─ RecordRow.tsx             # 履歴1行分の表示
├─ api/
│   └─ client.ts                 # fetchラッパー、baseURL管理
├─ types/
│   └─ index.ts                   # Candidate, Record型定義
└─ App.tsx / main.tsx              # ルーティング、選択中候補の状態管理
```

| コンポーネント | 責務 |
| --- | --- |
| IngredientInputPage | 食材の入力受付、送信、ローディング表示 |
| IngredientTagInput | 食材の追加・削除のUIロジック(親から値と更新関数を受け取る) |
| CandidateListPage | 5候補の表示、「これに決める」ボタン押下で選択状態を更新し詳細画面へ遷移 |
| CandidateCard | 1候補の表示と、選択ボタンのクリック通知(propsで受け渡し) |
| CandidateDetailPage | 選択済み候補の栄養情報表示、「この料理を作る」押下時のレシピ遷移+記録処理 |
| HistoryPage | 記録一覧の取得と表示、空状態の表示 |
| RecordRow | 1件の記録表示とレシピ遷移ボタン |
| client.ts | `/api/suggestions`・`/api/records`へのfetch呼び出しを共通化 |

選択中の候補(`selectedCandidate`)は`App.tsx`で状態管理し、`CandidateListPage`が更新、`CandidateDetailPage`が参照します。状態管理はReactの`useState`/`useEffect`のみで十分な規模のため、外部の状態管理ライブラリは導入しません。

## 処理詳細仕様

### SuggestionService.suggest(ingredients)

1. `ingredients`が空、または要素数が0件ならバリデーションエラーを投げる(呼び出し元のControllerでチェック済みの場合は省略可)
2. 食材リストから、Bedrock用のプロンプト文字列を組み立てる(例: 「食材: 卵, 玉ねぎ。作れる料理を5つ、name/descriptionを持つJSON配列のみで出力してください」)
3. `BedrockClient.invoke(prompt)`を呼び出す
4. 返却テキストをJSONとしてパースし、`List<AiCandidate>`(name, description, calories, fat, sugar, carbs)に変換する
5. パース失敗時は1回だけ再試行(同じプロンプトで再呼び出し)し、再度失敗したら`AiResponseException`を投げる
6. 各candidateの`name`から検索URLを組み立て、`recipeUrl`として付与する(URLエンコード必須)
7. `SuggestionResponse(candidates)`を返す

### RecordService.create(request)

1. `dishName`・`recipeUrl`・`cookedOn`が全て入力されているかを確認(空ならバリデーションエラー)
2. `CookingRecord`エンティティを組み立て、`CookingRecordRepository.save()`で保存する
3. 保存後のidを`RecordResponse(id)`として返す

### RecordService.list()

1. `CookingRecordRepository.findAllByOrderByCookedOnDesc()`で全件を日付降順取得する
2. 各エンティティを`RecordResponse`(id, dishName, recipeUrl, cookedOn)に変換して返す

## バリデーション仕様

### POST /api/suggestions

| 項目 | ルール |
| --- | --- |
| ingredients | 必須、要素数1〜10件 |
| 各食材の文字列 | 1〜50文字、空白のみは不可 |

### POST /api/records

| 項目 | ルール |
| --- | --- |
| dishName | 必須、1〜255文字 |
| recipeUrl | 必須、`http://`または`https://`で始まるURL形式 |
| cookedOn | 必須、`YYYY-MM-DD`形式。未来日はエラーとしない(前もって記録したいケースを許容) |

### 実装方針

Kotlinの`data class`にJakarta Bean Validationのアノテーション(`@NotBlank`, `@Size`, `@Pattern`など)を付与し、Controllerの引数に`@Valid`を付けることで、個別のif文を書かずにバリデーションを行います。

## 例外・エラーハンドリング詳細

`GlobalExceptionHandler`(`@ControllerAdvice`)で例外を一元的にHTTPレスポンスへ変換します。

| 例外 | 発生条件 | HTTPステータス | レスポンス | ログレベル |
| --- | --- | --- | --- | --- |
| MethodArgumentNotValidException | `@Valid`のバリデーション失敗 | 400 | `{"error": "<最初のエラーメッセージ>"}` | INFO |
| AiResponseException | Bedrock呼び出し失敗、またはJSONパース2回失敗 | 502 | `{"error": "候補の取得に失敗しました"}` | ERROR(スタックトレース含む) |
| RecordNotFoundException | (将来、記録の個別取得を追加した場合) 対象idが存在しない | 404 | `{"error": "指定された記録が見つかりません"}` | INFO |
| その他の想定外の例外 | ハンドラで捕捉していない例外全般 | 500 | `{"error": "サーバーエラーが発生しました"}` | ERROR(スタックトレース含む) |

フロントエンド側は、いずれのエラー応答でも`error`メッセージをそのままトースト等で表示し、ユーザーに再試行を促します。

## テスト観点(手動確認チェックリスト)

研修の時間内では自動テストの整備までは求めず、以下を手動で確認します。

- [ ] 食材を1件だけ入力して送信すると、候補が5件表示される
- [ ] 食材を複数件入力して送信すると、候補が5件表示される
- [ ] 食材を空のまま送信すると、エラーメッセージが表示される
- [ ] 候補の「これに決める」を押すと、候補詳細画面に遷移し栄養情報が表示される
- [ ] 候補詳細画面の「この料理を作る」を押すと、外部サイトが新しいタブで開き、同時に当日の日付で記録される
- [ ] 履歴画面に、記録した日付・料理名が新しい順で表示される
- [ ] 履歴画面で「レシピを見る」を押すと、記録時と同じレシピページに遷移する
- [ ] 記録が0件のとき、履歴画面に空状態のメッセージが表示される
- [ ] Bedrock呼び出しをあえて失敗させ(モデルID誤りなど)、エラーメッセージが表示されることを確認する

余裕があれば、`SuggestionService`のJSONパース処理のみ単体テストを追加すると良い経験になります。

## コーディング規約

| 対象 | 規約 |
| --- | --- |
| Kotlin命名 | クラス名はPascalCase、関数・変数はcamelCase、定数はUPPER_SNAKE_CASE |
| Kotlin lint | ktlintを導入し、デフォルトルールに従う |
| TypeScript命名 | コンポーネントはPascalCase(`CandidateCard.tsx`)、関数・変数はcamelCase |
| TypeScript lint/format | ESLint + Prettierを導入し、保存時整形を有効にする |
| コミットメッセージ | `feat: 候補提案APIを実装`のように、先頭に種別(feat/fix/docs/refactor等)を付ける |
| ブランチ運用 | `main`から各自`feature/xxx`を切り、Pull Requestでmainにマージする |
| レビュー | 時間が許せば、マージ前にClaude Codeへレビューを依頼する運用も体験してみる |

研修という位置づけのため、規約は最小限に留め、動くものを完成させることを優先します。
