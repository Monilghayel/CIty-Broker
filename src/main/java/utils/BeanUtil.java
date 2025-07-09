package utils;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.time.LocalDate;

@Named("beanUtil")
@RequestScoped
public class BeanUtil {

    // Returns today's date in yyyy-MM-dd format
    public String getTodayDate() {
        return LocalDate.now().toString();
    }

    // Returns tomorrow's date in yyyy-MM-dd format
    public String getTomorrowDate() {
        return LocalDate.now().plusDays(1).toString();
    }
}
