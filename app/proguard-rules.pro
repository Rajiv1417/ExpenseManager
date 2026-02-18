# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.expensemanager.data.local.entities.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }

# Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# iText
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# OpenCSV
-keep class com.opencsv.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

-dontwarn javax.xml.**
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**
