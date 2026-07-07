package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import database.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import model.NoteNode;
import repository.NoteRepository;
import service.TreeBuilder;
import service.TreeDragAndDrop;
public class NoteController {

    @FXML
    private TreeView<NoteNode> treeView;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea memoArea;

    private final NoteRepository repository = new NoteRepository();
    private final TreeBuilder treeBuilder = new TreeBuilder();

    @FXML
    public void initialize() {

        refreshTree();
        TreeDragAndDrop.setup(
                treeView,
                repository,
                this::refreshTree,
                sortModeButton::isSelected
        );
        treeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {

                    if (newVal == null) {
                        return;
                    }

                    NoteNode note = newVal.getValue();

                    titleField.setText(note.getTitle());
                    memoArea.setText(note.getMemo());
                }
        );
        sortModeButton.selectedProperty().addListener((obs, oldValue, newValue) -> {

            System.out.println("並び替えモード：" + newValue);

        });
                
    }
    private void loadTree() {

        List<NoteNode> notes = repository.findAll();

        TreeItem<NoteNode> root = treeBuilder.build(notes);

        treeView.setRoot(root);

        if (root != null) {
            root.setExpanded(true);
        }

    }

    private void showNote(NoteNode note) {
        titleField.setText(note.getTitle());
        memoArea.setText(note.getMemo());
    }
    
    private void refreshTree() {
    	List<NoteNode> notes = repository.findAll();

    	for (NoteNode note : notes) {
    	    System.out.println(
    	        "id=" + note.getId()
    	        + " parent=" + note.getParentId()
    	        + " title=" + note.getTitle()
    	    );
    	}

        TreeItem<NoteNode> root = treeBuilder.build(repository.findAll());

        treeView.setRoot(root);

        if (root != null) {
            root.setExpanded(true);
        }
        treeView.getSelectionModel().clearSelection();
    }
    
    public void update(NoteNode note) {

        String sql = """
            UPDATE note
            SET title = ?,
                memo = ?,
                updated_at = datetime('now')
            WHERE id = ?
            """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, note.getTitle());
            ps.setString(2, note.getMemo());
            ps.setInt(3, note.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSave() {
        TreeItem<NoteNode> selectedItem =
                treeView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            System.out.println("ツリーからノートを選択してください");
            return;
        }

        NoteNode selectedNote = selectedItem.getValue();

        selectedNote.setTitle(titleField.getText());
        selectedNote.setMemo(memoArea.getText());

        repository.update(selectedNote);

        // TreeViewに表示されるタイトルを更新するため再読み込み
        refreshTree();
    }
    @FXML
    private void onAdd() {

        TreeItem<NoteNode> selected = treeView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        NoteNode parent = selected.getValue();

        repository.insert(parent.getId(), "新しいノート");

        refreshTree();
    }
    @FXML
    private void onDelete() {
    	

        TreeItem<NoteNode> selectedItem =
                treeView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            System.out.println("ノートを選択してください");
            return;
        }

        NoteNode selectedNote = selectedItem.getValue();

        // ルートノートは削除できない
        if (selectedNote.getParentId() == null) {
            System.out.println("ルートノートは削除できません");
            return;
        }

     // ★確認ダイアログ
        ButtonType deleteButton = new ButtonType("削除する");
        ButtonType cancelButton = new ButtonType("やめる", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle("削除確認");
        alert.setHeaderText("「" + selectedNote.getTitle() + "」を削除しますか？");
        alert.setContentText(
                "子ノードもすべて削除されます。\n\n" +
                "この操作は元に戻せません。"
        );

        // デフォルトボタンを置き換える
        alert.getButtonTypes().setAll(deleteButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != deleteButton) {
            return;
        }

        deleteRecursive(selectedNote.getId());
        

        refreshTree();
    }
    private void deleteRecursive(int id) {

        // 子ノードを取得
        List<NoteNode> children = repository.findChildren(id);

        // 子を先に削除
        for (NoteNode child : children) {
            deleteRecursive(child.getId());
        }

        // 最後に自分を削除
        repository.delete(id);
    }
    @FXML
    private ToggleButton sortModeButton;
    @FXML
    private void onMove() {

        TreeItem<NoteNode> selectedItem =
                treeView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            System.out.println("ノートを選択してください");
            return;
        }

        NoteNode selectedNote = selectedItem.getValue();

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("親変更");
        dialog.setHeaderText("移動先の親IDを入力してください");
        dialog.setContentText("親ID：");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        try {

            int newParentId = Integer.parseInt(result.get());

            repository.move(selectedNote.getId(), newParentId);

            refreshTree();

        } catch (NumberFormatException e) {

            System.out.println("数字を入力してください");

        }
    }
}