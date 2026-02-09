# 要員アサイン管理システム (Staff Assignment System)

## 1. プロジェクト概要 (Project Overview)
本システムは、企業内のプロジェクトリソース管理、要員アサイン計画、および予実管理を効率化するために設計された **Webベースの統合管理プラットフォーム** です。
従来のExcelベースの管理から脱却し、**データの一元化**、**リアルタイムなコスト分析**、**アサイン衝突の自動検知** を実現します。

### ターゲット・ソリューション
*   **リソース最適化**: プロジェクトごとの要員配置状況を可視化し、リソースの過不足を即座に特定。
*   **コスト管理**: 計画予算(Project Plan)と実際のアサイン(Assignment)に基づくコスト乖離をリアルタイムで集計。
*   **運用効率化**: CSVによる一括入出力機能を全マスタ・トランザクションに実装し、既存データからの移行や他システム連携を容易化。

---

## 2. システムアーキテクチャ (System Architecture)
本システムは、保守性と拡張性を重視した **前後端分離 (SPA + REST API)** アーキテクチャを採用しています。

### 2.1 技術スタック (Technology Stack)

#### Frontend (SPA)
*   **Framework**: Vue.js 3 (Composition API / Script Setup) - 高い再利用性と型推論への親和性。
*   **Build Tool**: Vite - 高速なHMRとビルドパフォーマンス。
*   **UI Library**: Element Plus - 統一感のあるエンタープライズ向けUIコンポーネント。
*   **State Management**: Pinia - 軽量かつ直感的な状態管理。
*   **HTTP Client**: Axios - インターセプターによるJWTトークン管理と統一的なエラーハンドリング。

#### Backend (REST API)
*   **Framework**: Spring Boot 3.2.2 - 最新のJavaエコシステムに基づいた堅牢なバックエンド。
*   **Language**: Java 21 (LTS) - Virtual Threads等の最新機能への対応を見据えた選定。
*   **ORM**: MyBatis 3.5 - 複雑なSQLクエリ（動的報表、多重結合）に対する高い制御性。
*   **Security**: Spring Security + JWT (Stateless Authentication) - セッションレスな認証基盤。
*   **Data Processing**: OpenCSV - 大量データのインポート/エクスポート処理。
*   **API Documentation**: RESTful設計原則に基づく統一的なインターフェース。

#### Infrastructure & Database
*   **Database**: PostgreSQL 16 - 堅牢なトランザクション管理と高度なクエリ機能。
*   **Containerization**: Docker & Docker Compose - 開発・本番環境の等価性を保証するコンテナオーケストレーション。
*   **Web Server**: Nginx (Alpine) - フロントエンド配信およびリバースプロキシ。

### 2.2 アーキテクチャ図 (Conceptual Diagram)
```mermaid
graph TD
    Client[Browser (Vue 3)] <-->|REST API (JSON)| Nginx[Nginx Reverse Proxy]
    Nginx <-->|Port 8080| Backend[Spring Boot Backend]
    Backend <-->|JDBC| DB[(PostgreSQL 16)]
    
    subgraph Backend Layer
        Controller[Controller Layer\n(REST Endpoint / Validation)]
        Service[Service Layer\n(Business Logic / Transaction)]
        Mapper[Mapper Layer\n(MyBatis SQL Execution)]
        Controller --> Service --> Mapper
    end
```

---

## 3. 実装のポイントと技術的特徴 (Technical Highlights)

### 3.1 堅牢なセキュリティ設計
*   **JWT (JSON Web Token)** を用いたステートレス認証を採用。
*   **RBAC (Role-Based Access Control)**: `@PreAuthorize("hasRole('ADMIN')")` アノテーションによるメソッドレベルの権限管理（管理者のみがCSV操作やマスタ更新可能）。
*   **パスワードハッシュ化**: BCryptアルゴリズムによる安全な認証情報管理。

### 3.2 統一されたAPIレスポンスと例外処理
*   **Global Exception Handling**: `@RestControllerAdvice` により、バリデーションエラー(`MethodArgumentNotValidException`)やビジネス例外(`BusinessException`)を一元的に捕捉。
*   **Unified Response Format**: 全てのAPIレスポンスを `ApiResponse<T>` ラッパーで統一し、フロントエンド側でのエラーハンドリングロジックを簡素化。

### 3.3 データ整合性とトランザクション管理
*   **Transactional**: サービス層での `@Transactional` 制御により、CSV一括インポート時などの原子性(Atomicity)を保証。
*   **Upsert Logic**: データインポートにおいて、既存データの更新と新規データの挿入を自動判別するロジックを実装。
*   **Business Validation**: アサイン登録時の「開始年月 > 終了年月」チェックや、外部キー制約の事前検証。

### 3.4 動的レポート生成
*   MyBatisの `<if>` タグ等を活用した動的SQLにより、検索条件（プロジェクトID、社員ID、年月範囲）に応じた柔軟なデータ抽出を実現。
*   **BOM付きUTF-8 CSV出力**: 日本語環境（Excel等）での文字化けを防止するストリーム処理。

---

## 4. データベース設計 (Database Schema)

主要なエンティティ関係は以下の通りです。

*   **project_master**: プロジェクト基本情報（親プロジェクト概念あり）。
*   **project_plan**: プロジェクトごとの月次計画予算（Version管理）。
*   **employee_master**: 社員情報。
*   **assignment**: アサイン実績（Project N : M Employee）。
*   **user_master**: システム利用ユーザー（認証・権限）。
*   **code_master**: 汎用コード（区分値管理）。

---

## 5. 環境構築と実行 (Setup & Execution)

### 5.1 必要要件 (Prerequisites)
*   **Docker Desktop** (または Docker Engine + Docker Compose Plugin)
*   Ports: `80` (Frontend), `8080` (Backend), `5433` (Database) が空いていること。

### 5.2 本番/検証環境の起動 (Production Mode)
コンテナオーケストレーションにより、全サービスを一括起動します。

```bash
# プロジェクトルートで実行
docker-compose up -d --build
```

*   **Access**: [http://localhost](http://localhost)
*   **Database**: `localhost:5433` (User: `postgres` / Pass: `postgres`)

### 5.3 開発環境の起動 (Development Mode)
コードの変更を即座に反映させたい場合の手順です。

**Backend (Java/Gradle)**
```bash
cd backend
./gradlew bootRun
```

**Frontend (Node/Vite)**
```bash
cd frontend
npm install
npm run dev
```

### 5.4 初期アカウント
*   **管理者 (Admin)**: `admin@example.com` / `password`
*   **一般 (User)**: `user@example.com` / `password`

---

## 6. ディレクトリ構造 (Directory Structure)

```
/
├── backend/                  # Spring Boot Application
│   ├── src/main/java/.../
│   │   ├── config/           # Security, WebMvc, Swagger Config
│   │   ├── controller/       # REST Controllers
│   │   ├── service/          # Business Logic
│   │   ├── repository/       # MyBatis Mappers
│   │   ├── entity/           # Domain Models
│   │   ├── dto/              # Data Transfer Objects (CSV/API)
│   │   ├── exception/        # Global Exception Handlers
│   │   └── security/         # JWT Provider, Auth Filters
│   └── src/main/resources/
│       └── mybatis/mapper/   # SQL XML definitions
│
├── frontend/                 # Vue.js Application
│   ├── src/
│   │   ├── api/              # Axios API definitions
│   │   ├── components/       # Reusable UI Components
│   │   ├── stores/           # Pinia Global State
│   │   └── views/            # Page Views
│
├── init-data/                # Database Initialization Scripts (Schema & Data)
└── docker-compose.yml        # Container Orchestration Config
```
