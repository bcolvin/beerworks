# Keep generic signatures and annotations for Gson TypeToken and Room
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Gson TypeToken and reflection support
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep seeder JSON DTOs and model classes deserialized via Gson reflection
-keep class com.bc.beerworks.data.local.seeder.** { *; }
-keep class com.bc.beerworks.data.model.CommercialYeastEquivalent { *; }

# Keep Room entities and database classes
-keep class com.bc.beerworks.data.local.entity.** { *; }
