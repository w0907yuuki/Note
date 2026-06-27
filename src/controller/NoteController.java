package controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import model.NoteNode;
import repository.NoteRepository;

public class NoteController {

    @FXML
    private TreeView<NoteNode> treeView;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea memoArea;

    private final NoteRepository repository = new NoteRepository();

    @FXML
    public void initialize() {
        loadTree();

        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldItem, newItem) -> {
                    if (newItem != null) {
                        showNote(newItem.getValue());
                    }
                });
    }

    private void loadTree() {
        List<NoteNode> notes = repository.findAll();

        Map<Integer, TreeItem<NoteNode>> itemMap = new HashMap<>();
        TreeItem<NoteNode> rootItem = null;

        // 先に全ノードをTreeItemへ変換
        for (NoteNode note : notes) {
            TreeItem<NoteNode> item = new TreeItem<>(note);
            itemMap.put(note.getId(), item);

            // parent_idがnullのノードをルートにする
            if (note.getParentId() == null) {
                rootItem = item;
            }
        }

        // 親子関係を作る
        for (NoteNode note : notes) {
            if (note.getParentId() != null) {
                TreeItem<NoteNode> parentItem = itemMap.get(note.getParentId());
                TreeItem<NoteNode> childItem = itemMap.get(note.getId());

                if (parentItem != null && childItem != null) {
                    parentItem.getChildren().add(childItem);
                }
            }
        }

        if (rootItem != null) {
            rootItem.setExpanded(true);
            treeView.setRoot(rootItem);
            treeView.setShowRoot(true);
        }
    }

    private void showNote(NoteNode note) {
        titleField.setText(note.getTitle());
        memoArea.setText(note.getMemo());
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
        loadTree();
    }
    @FXML
    private void onAdd() {
        System.out.println("追加");
    }

    @FXML
    private void onDelete() {
        System.out.println("削除");
    }

}