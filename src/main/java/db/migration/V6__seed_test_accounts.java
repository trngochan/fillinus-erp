package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;

/**
 * V6 — Seed test accounts with properly BCrypt-hashed passwords.
 * Uses BCryptPasswordEncoder directly (no Spring context needed).
 *
 * Accounts seeded:
 *   admin    / Admin@123456   (ADMIN)
 *   manager  / Manager@123   (MANAGER)
 *   emp01    / Employee@123  (EMPLOYEE)
 */
public class V6__seed_test_accounts extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String[][] accounts = {
            // { username, rawPassword, fullName, email, roleName }
            { "admin",   "Admin@123456", "System Administrator", "admin@fillinus.com",   "ADMIN"    },
            { "manager", "Manager@123",  "Test Manager",          "manager@fillinus.com", "MANAGER"  },
            { "emp01",   "Employee@123", "Test Employee",         "emp01@fillinus.com",   "EMPLOYEE" },
        };

        String sql = """
            INSERT INTO users (username, password, full_name, email, role_id, is_active)
            SELECT ?, ?, ?, ?, r.id, TRUE
            FROM   roles r
            WHERE  r.name = ?
            ON CONFLICT (username) DO UPDATE
                SET password   = EXCLUDED.password,
                    full_name  = EXCLUDED.full_name,
                    updated_at = NOW()
            """;

        for (String[] account : accounts) {
            String hashedPassword = encoder.encode(account[1]);
            try (PreparedStatement stmt = context.getConnection().prepareStatement(sql)) {
                stmt.setString(1, account[0]); // username
                stmt.setString(2, hashedPassword);
                stmt.setString(3, account[2]); // full_name
                stmt.setString(4, account[3]); // email
                stmt.setString(5, account[4]); // role name
                stmt.executeUpdate();
            }
        }
    }
}
