/**
 * Firebase Cloud Function to send Email OTP
 *
 * Requirement:
 * 1. npm install @sendgrid/mail
 * 2. Setup SendGrid API Key in Firebase Config
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const sgMail = require('@sendgrid/mail');

admin.initializeApp();
sgMail.setApiKey('YOUR_SENDGRID_API_KEY'); // Or use functions.config().sendgrid.key

exports.sendEmailOtpTrigger = functions.firestore
    .document('email_otps/{userId}')
    .onWrite(async (change, context) => {
        const data = change.after.data();

        // Only send if it's a new OTP and not verified yet
        if (!data || data.verified === true) {
            return null;
        }

        console.log("Sending OTP to:", data.email);
        console.log("Generated OTP:", data.otp);

        const msg = {
            to: data.email,
            from: 'noreply@meditime.com', // Verified sender in SendGrid
            subject: 'Your MediTime Verification Code',
            text: `Your verification code is: ${data.otp}\nThis code expires in 40 seconds.`,
            html: `<strong>Your verification code is: ${data.otp}</strong><br>This code expires in 40 seconds.`,
        };

        try {
            await sgMail.send(msg);
            console.log("Email sent successfully");
        } catch (error) {
            console.error("Error sending email:", error);
            if (error.response) {
                console.error(error.response.body);
            }
        }
        return null;
    });
