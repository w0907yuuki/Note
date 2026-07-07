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

    // 日時文字列をLocalDateTimeへ変換
    private LocalDateTime parseDateTime(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(value);
    }

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

                NoteNode note = new NoteNode(
                        rs.getInt("id"),
                        parentId,
                        rs.getString("title"),
                        rs.getString("memo"),
                        rs.getInt("sort_order"),
                        parseDateTime(rs.getString("created_at")),
                        parseDateTime(rs.getString("updated_at"))
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

                return new NoteNode(
                        rs.getInt("id"),
                        null,
                        rs.getString("title"),
                        rs.getString("memo"),
                        rs.getInt("sort_order"),
                        parseDateTime(rs.getString("created_at")),
                        parseDateTime(rs.getString("updated_at"))
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
                SET title = ?,
                    memo = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, note.getTitle());
            ps.setString(2, note.getMemo());
            ps.setString(3, LocalDateTime.now().toString());
            ps.setInt(4, note.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean hasChildren(int id) {

        String sql = """
                SELECT COUNT(*)
                FROM note
                WHERE parent_id = ?
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
 // 子ノード一覧取得
    public List<NoteNode> findChildren(int parentId) {

        List<NoteNode> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM note
                WHERE parent_id = ?
                ORDER BY sort_order, id
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, parentId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Integer parent = rs.getObject("parent_id") == null
                        ? null
                        : rs.getInt("parent_id");

                NoteNode note = new NoteNode(
                        rs.getInt("id"),
                        parent,
                        rs.getString("title"),
                        rs.getString("memo"),
                        rs.getInt("sort_order"),
                        parseDateTime(rs.getString("created_at")),
                        parseDateTime(rs.getString("updated_at"))
                );

                list.add(note);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
 // 親を変更
    public void move(int id, Integer newParentId) {

        String sql = """
                UPDATE note
                SET parent_id = ?
                WHERE id = ?
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (newParentId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, newParentId);
            }

            ps.setInt(2, id);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 // 並び順更新
    public void updateSortOrders(List<NoteNode> notes) {

        String sql = """
                UPDATE note
                SET sort_order = ?
                WHERE id = ?
                """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < notes.size(); i++) {

                ps.setInt(1, i);
                ps.setInt(2, notes.get(i).getId());

                ps.addBatch();
            }

            ps.executeBatch();

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
