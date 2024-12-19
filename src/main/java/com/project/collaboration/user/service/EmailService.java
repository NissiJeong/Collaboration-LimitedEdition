package com.project.collaboration.user.service;

import com.project.collaboration.user.repository.RedisRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisRepository redisRepository;
    private static final String senderEmail = "jnissi92@gmail.com";

    public EmailService(JavaMailSender javaMailSender, RedisRepository redisRepository) {
        this.javaMailSender = javaMailSender;
        this.redisRepository = redisRepository;
    }

    private String createCode() {
        int leftLimit = 48; // number '0'
        int rightLimit = 122; // alphabet 'z'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 | i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    // 인증코드 이메일 발송
    public void sendEmail(String toEmail) throws MessagingException {
        if (redisRepository.existData(toEmail)) {
            redisRepository.deleteData(toEmail);
        }
        // 이메일 폼 생성
        MimeMessage emailForm = createEmailForm(toEmail);
        // 이메일 발송
        javaMailSender.send(emailForm);
    }

    // 이메일 폼 생성
    private MimeMessage createEmailForm(String email) throws MessagingException {
        String authCode = createCode();

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("안녕하세요. 인증번호입니다.");
        message.setFrom(senderEmail);
        message.setText(setContext(authCode), "utf-8", "html");

        // Redis 에 해당 인증코드 인증 시간 설정
        redisRepository.setDataExpire(email, authCode, 60 * 30L);

        return message;
    }

    // 이메일 내용 초기화
    private String setContext(String code) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <body>
                <div style="margin:120px">
                    <div style="margin-bottom: 10px">
                        <h1>인증 코드 메일입니다.</h1>
                        <br/>
                        <h3 style="text-align: center;"> 아래 코드를 사이트에 입력해주십시오</h3>
                    </div>
                    <div style="text-align: center;">
                        <h2 style="color: crimson;">%s</h2>
                    </div>
                    <br/>
                </div>
                </body>
                </html>
        """, code);
    }

    public boolean verifyEmailCode(String email, String verifyCode) {
        String codeFoundByEmail = redisRepository.getData(email);
        if (codeFoundByEmail == null) {
            return false;
        }
        boolean isVerify = codeFoundByEmail.equals(verifyCode);
        if(isVerify) {
            redisRepository.deleteData(email);
        }
        return isVerify;
    }
}
