import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.settermjd.loginverification.entity.User;

public class UserTest {
    @Test
    void canInstantiateObject() {
        User user = new User(
                1,
                "settermjd",
                "Matthew",
                "Setter",
                "matthew@matthewsetter.com",
                "+61493856711"
        );
        assertInstanceOf(User.class, user);
    }
}
