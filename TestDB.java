import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/eduapp", "postgres",
                    "password");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT id, image_url, requires_image_display FROM questions ORDER BY id DESC LIMIT 5");
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("id") + " | " +
                        "IMG: " + rs.getString("image_url") + " | " +
                        "SHOW: " + rs.getBoolean("requires_image_display"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
