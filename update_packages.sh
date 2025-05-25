#!/bin/bash

# Update package declarations in all Kotlin files
find src/main/kotlin/com/biding -type f -name "*.kt" -exec sed -i '' 's/package com.biding.test/package com.biding/g' {} \;

# Update imports in all Kotlin files
find src/main/kotlin/com/biding -type f -name "*.kt" -exec sed -i '' 's/import com.biding.test/import com.biding/g' {} \;
