package com.biding.db.exception

class DuplicatePhoneNumberException(phoneNumber: String) : 
    RuntimeException("Phone number $phoneNumber is already registered")
