# 💰 Expense Manager - Android App

A full-featured personal finance app built with **Kotlin + Jetpack Compose + MVVM + Room + Hilt + WorkManager**, similar to Wallet by BudgetBakers — with extra features.

---

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35 (minSdk 26)

### Steps
1. Clone / extract this project
2. Open in Android Studio
3. Let Gradle sync (first sync downloads ~500MB of dependencies)
4. Run on emulator or device (API 26+)

---

## 📁 Project Structure

```
app/src/main/java/com/expensemanager/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database
│   │   ├── Converters.kt           # Type converters
│   │   ├── DatabaseInitializer.kt  # Seeds default categories/accounts
│   │   ├── dao/
│   │   │   ├── TransactionDao.kt
│   │   │   ├── AccountDao.kt
│   │   │   └── CategoryDao.kt
│   │   └── entities/
│   │       ├── TransactionEntity.kt
│   │       ├── AccountEntity.kt
│   │       ├── CategoryEntity.kt
│   │       └── RecurringTransactionEntity.kt
│   └── repository/
│       ├── TransactionRepository.kt
│       ├── AccountRepository.kt
│       └── CategoryRepository.kt
├── di/
│   └── AppModule.kt                # Hilt modules
├── domain/
│   └── usecase/
│       └── TransactionUseCases.kt  # AddTransaction, LinkRefund, Import...
├── ui/
│   ├── screens/
│   │   ├── dashboard/              # Home screen with charts
│   │   ├── addtransaction/         # Add/Edit transaction form
│   │   ├── accounts/               # Accounts list & management
│   │   ├── import_screen/          # File import with column mapping
│   │   └── settings/               # Dark mode, export, notifications
│   ├── components/
│   │   └── TransactionItem.kt      # Reusable transaction card with refund badge
│   ├── theme/                      # Material3 colors, typography
│   └── Navigation.kt               # NavHost + Routes
├── utils/
│   ├── SmsParser.kt                # Regex SMS parser (SBI, HDFC, ICICI, Paytm, GPay)
│   ├── SmsReceiver.kt              # BroadcastReceiver for incoming SMS
│   ├── FileImporter.kt             # CSV / Excel / PDF parsers
│   ├── OcrHelper.kt                # ML Kit OCR for receipts
│   ├── ExportHelper.kt             # CSV / Excel / PDF export + share
│   └── CurrencyFormatter.kt        # INR formatting
├── workers/
│   └── RecurringTransactionWorker.kt # WorkManager for recurring transactions
├── ExpenseManagerApp.kt            # Hilt application class
└── MainActivity.kt
```

---

## ✅ Features

### 1. Manual Transaction Entry
- **3 types**: Expense | Income | Transfer (tab selector with color coding)
- Fields: Amount, Account, Category, Date/Time, Notes, Payee/Payer, Labels, Payment Type, Status
- **Cashback/Refund Linking** — exactly like the screenshot you provided:
  - Add a partial or full refund/cashback to any expense
  - The cashback appears as a separate INCOME entry in the chosen wallet/account
  - The original transaction shows ~~₹2000~~ → ₹1850 (with "Cashback ₹150" tag in green, linked)

### 2. Import Transactions
- **CSV** (OpenCSV), **Excel** (Apache POI), **PDF** (iText)
- Column mapping UI — first import shows a mapping screen
- Auto-detection of common bank statement column names (SBI, HDFC, ICICI formats)
- Bulk insert with account balance updates

### 3. SMS Parsing & Auto-Detection
- `SmsReceiver` BroadcastReceiver listens for incoming SMS
- Regex patterns for **SBI, HDFC, ICICI, Axis, Paytm, GPay, PhonePe, PNB, Kotak**
- Extracts: Amount, Account last 4 digits, Merchant/Payee, Balance, Bank name
- Auto-saved as **PENDING** — shown as banner on dashboard for user review/edit
- Manual SMS scan available via `SmsReader.readRecentTransactionSms()`

### 4. Receipt OCR
- Uses **Google ML Kit Text Recognition**
- Share any receipt image/PDF into the app
- Extracts Amount, Merchant, Date → pre-fills Add Transaction form

### 5. Dashboard
- **Balance Overview Card** with gradient — Total Balance, Monthly Income, Monthly Expense
- **Accounts horizontal scroll** with balance chips
- **Spending Bar Chart** (daily breakdown)
- **Recent Transactions** list with cashback badge
- Pending SMS transactions banner

### 6. Accounts
- Full CRUD for accounts (Bank, Cash, Wallet, Credit Card, etc.)
- Net Worth card
- Balance auto-updated on every transaction

### 7. Categories
- 15 default expense categories, 9 default income categories
- Custom categories with icons and colors

### 8. Recurring Transactions
- WorkManager `PeriodicWorkRequest` with configurable interval (days)
- System notification on auto-recording

### 9. Export
- CSV, Excel, PDF — all via share intent
- Available in Settings screen

### 10. Dark Mode
- Full Material3 dynamic color + manual dark mode toggle

---

## 🧪 Tests

```
app/src/test/java/com/expensemanager/
├── utils/
│   ├── SmsParserTest.kt       # 9 test cases (SBI, HDFC, ICICI, Paytm, GPay, edge cases)
│   ├── CurrencyFormatterTest.kt
│   └── ImportMappingTest.kt   # Column auto-detection tests
```

Run with:
```bash
./gradlew test
```

---

## 🔧 SMS Regex Patterns

| Bank | Sample SMS |
|------|------------|
| **SBI** | `INR 2,500.00 debited from A/c No. XX1234 on 18-02-26. Info: UPI/SWIGGY. Avail Bal: INR 15,000.00.` |
| **HDFC** | `Rs.1,200.00 debited from your HDFC Bank A/c **5678 for purchase at AMAZON. Available Bal: Rs.8,000.00` |
| **ICICI** | `ICICI Bank Acct XX4321 debited for Rs 500 on 18-Feb-26. Info: UPI-PhonePe/ZOMATO` |
| **Paytm** | `You have paid Rs.350.00 to Dominos Pizza via Paytm on 18-Feb-26 13:45:00.` |
| **GPay** | `Rs. 150 sent via Google Pay to PhonePe on 18 Feb 2026. UPI ID: merchant@ok` |

---

## 📦 Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose | BOM 2024.09 | UI |
| Material3 | latest | Design system |
| Hilt | 2.52 | DI |
| Room | 2.6.1 | Database |
| Navigation Compose | 2.8.1 | Navigation |
| WorkManager | 2.9.1 | Recurring transactions |
| ML Kit Text Recognition | 19.0 | Receipt OCR |
| OpenCSV | 5.9 | CSV import/export |
| Apache POI | 5.2.5 | Excel import/export |
| iText | 7.2.5 | PDF import/export |
| Vico | 1.6.7 | Charts |
| Accompanist Permissions | 0.36 | Runtime permissions |

---

## 🔮 Future Enhancements (Sync-Ready)
- Cloud sync via Firebase / REST API (Repository pattern ready)
- Budget goals per category
- Net worth tracking with investments
- Multi-currency support
- Biometric lock

---

## 🛠 Building from GitHub / Command Line

### First-time setup (downloads the real `gradle-wrapper.jar`)
```bash
git clone https://github.com/yourname/ExpenseManager.git
cd ExpenseManager

# The gradlew script auto-downloads gradle-wrapper.jar on first run:
chmod +x gradlew
./gradlew assembleDebug
```

### Manual wrapper bootstrap (if auto-download fails)
```bash
curl -sL https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar \
  -o gradle/wrapper/gradle-wrapper.jar

./gradlew assembleDebug
```

### Common Gradle tasks
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew lintDebug              # Run lint checks
./gradlew installDebug           # Install on connected device
./gradlew clean build            # Clean build

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions CI
Push to `main` or `develop` → automatically:
- Downloads gradle-wrapper.jar
- Runs unit tests
- Builds debug APK
- Uploads APK as artifact (`.github/workflows/android-ci.yml`)

### Opening in Android Studio
1. Open Android Studio → **Open** → select the `ExpenseManager/` folder
2. Android Studio auto-detects it as a Gradle project
3. It will download the real `gradle-wrapper.jar` automatically
4. Let sync complete (~2-5 min first time)
5. Run → select device → ▶
