package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import model.NoteNode;

public class NoteRepository {

    // 全ノート取得
    public List<NoteNode> findAll() {

        List<NoteNode> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM note
                ORDER BY parent_id, sort_order, id
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Integer parentId = rs.getObject("parent_id") == null
                        ? null
                        : rs.getInt("parent_id");

                LocalDateTime createdAt = rs.getString("created_at") == null
                        ? null
                        : LocalDateTime.parse(rs.getString("created_at"));

                LocalDateTime updatedAt = rs.getString("updated_at") == null
                        ? null
                        : LocalDateTime.parse(rs.getString("updated_at"));

                NoteNode note = new NoteNode(
                        rs.getInt("id"),
                        parentId,
                        rs.getString("title"),
                        rs.getString("memo"),
                        rs.getInt("sort_order"),
                        createdAt,
                        updatedAt
                );

                list.add(note);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ルートノート取得
    public NoteNode findRoot() {

        String sql = """
                SELECT *
                FROM note
                WHERE parent_id IS NULL
                LIMIT 1
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                LocalDateTime createdAt = rs.getString("created_at") == null
                        ? null
                        : LocalDateTime.parse(rs.getString("created_at"));

                LocalDateTime updatedAt = rs.getString("updated_at") == null
                        ? null
                        : LocalDateTime.parse(rs.getString("updated_at"));

                return new NoteNode(
                        rs.getInt("id"),
                        null,
                        rs.getString("title"),
                        rs.getString("memo"),
                        rs.getInt("sort_order"),
                        createdAt,
                        updatedAt
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ノート追加
    public void insert(Integer parentId, String title) {

        String sql = """
                INSERT INTO note
                (
                    parent_id,
                    title,
                    memo,
                    sort_order,
                    created_at,
                    updated_at
                )
                VALUES
                (?, ?, '', 0, ?, ?)
                """;

        LocalDateTime now = LocalDateTime.now();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (parentId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, parentId);
            }

            ps.setString(2, title);
            ps.setString(3, now.toString());
            ps.setString(4, now.toString());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 更新
    public void update(NoteNode note) {

        String sql = """
                UPDATE note
                SET
                    title = ?,
                    memo = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        LocalDateTime now = LocalDateTime.now();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, note.getTitle());
            ps.setString(2, note.getMemo());
            ps.setString(3, now.toString());
            ps.setInt(4, note.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 削除
    public void delete(int id) {

        String sql = "DELETE FROM note WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}