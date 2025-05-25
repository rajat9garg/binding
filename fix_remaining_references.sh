#!/bin/bash

# Update fully qualified package references in all Kotlin files
find src/main/kotlin/com/biding -type f -name "*.kt" -exec sed -i '' 's/com\.biding\.test\./com.biding./g' {} \;
