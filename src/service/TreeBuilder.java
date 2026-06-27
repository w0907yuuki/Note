package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.TreeItem;
import model.NoteNode;

public class TreeBuilder {

    public TreeItem<NoteNode> build(List<NoteNode> notes) {

        Map<Integer, TreeItem<NoteNode>> map = new HashMap<>();

        TreeItem<NoteNode> root = null;

        // 全ノードを生成
        for (NoteNode note : notes) {

            TreeItem<NoteNode> item = new TreeItem<>(note);

            map.put(note.getId(), item);

        }

        // 親子関係を作成
        for (NoteNode note : notes) {

            TreeItem<NoteNode> item = map.get(note.getId());

            if (note.getParentId() == null) {

                root = item;

            } else {

                TreeItem<NoteNode> parent =
                        map.get(note.getParentId());

                if (parent != null) {
                    parent.getChildren().add(item);
                }

            }

        }

        return root;

    }

}