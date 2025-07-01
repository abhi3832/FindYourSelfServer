package com.example.otpService

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.rest.lookups.v1.PhoneNumber
import io.github.cdimascio.dotenv.dotenv

data class OtpEntry(
    val otp : String,
    val expiry : Long
)

object TwilioService {

    val dotenv = dotenv()

    private val accountSid = dotenv["TWILIO_ACCOUNT_SID"]
    private val authToken = dotenv["TWILIO_AUTH_TOKEN"]
    private val fromPhoneNumber = dotenv["TWILIO_FROM_PHONE_NUMBER"]

    init{
        Twilio.init(accountSid, authToken)
    }

    fun sendOtp(toPhoneNumber: String, otp: String) : String {
       val message = Message.creator(
           com.twilio.type.PhoneNumber(toPhoneNumber),
           com.twilio.type.PhoneNumber(fromPhoneNumber),
           "Your OTP is $otp."
       ).create()

        return message.sid
    }

    fun verifyOtp(phone : String, inputOtp : String) : Boolean {
        val entry : OtpEntry? = null // get actual otp from db

        if(entry == null || System.currentTimeMillis() > entry.expiry){
            // remove the otp from db
            return false
        }

        val isValid = entry.otp == inputOtp

        if(isValid){
            // remove the otp from db
        }
        return isValid
    }
}