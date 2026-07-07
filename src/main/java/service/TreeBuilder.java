package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.TreeItem;
import model.NoteNode;

public class TreeBuilder {

    public TreeItem<NoteNode> build(List<NoteNode> notes) {

        Map<Integer, TreeItem<NoteNode>> itemMap = new HashMap<>();

        TreeItem<NoteNode> root = null;

        // 1. 全ノードをTreeItem化
        for (NoteNode note : notes) {
            itemMap.put(note.getId(), new TreeItem<>(note));
        }

        // 2. 親子関係を構築
        for (NoteNode note : notes) {

            TreeItem<NoteNode> item = itemMap.get(note.getId());

            if (note.getParentId() == null) {
                root = item;
            } else {
                TreeItem<NoteNode> parent = itemMap.get(note.getParentId());

                if (parent != null) {
                    parent.getChildren().add(item);
                }
            }
        }

        // 3. sort_orderで並び替え（兄弟ノード単位）
        for (TreeItem<NoteNode> item : itemMap.values()) {

            item.getChildren().sort((a, b) -> {

                int aOrder = a.getValue().getSortOrder();
                int bOrder = b.getValue().getSortOrder();

                return Integer.compare(aOrder, bOrder);
            });
        }

        return root;
    }
}