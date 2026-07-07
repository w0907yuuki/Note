package service;

import java.util.List;
import java.util.function.BooleanSupplier;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import model.NoteNode;
import repository.NoteRepository;
public class TreeDragAndDrop {

    private static TreeItem<NoteNode> draggedItem;
    
    private static DropPosition dropPosition;

    public static void setup(
            TreeView<NoteNode> treeView,
            NoteRepository repository,
            Runnable refreshTree,
            BooleanSupplier isSortMode) {

        treeView.setCellFactory(tv -> {

            TreeCell<NoteNode> cell = new TreeCell<>() {

                @Override
                protected void updateItem(NoteNode item, boolean empty) {

                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getTitle());
                    }
                }
            };

            cell.setOnDragDetected(event -> {

            	if (!isSortMode.getAsBoolean()) {
            	    return;
            	}
                if (cell.isEmpty()) {
                    return;
                }

                draggedItem = cell.getTreeItem();

                Dragboard db =
                        cell.startDragAndDrop(TransferMode.MOVE);

                ClipboardContent content =
                        new ClipboardContent();

                content.putString(
                        String.valueOf(
                                draggedItem.getValue().getId()));

                db.setContent(content);

                event.consume();
            });
            cell.setOnDragOver(event -> {

                if (!isSortMode.getAsBoolean()) {
                    return;
                }

                if (event.getGestureSource() != cell
                        && event.getDragboard().hasString()) {

                    double y = event.getY();
                    double height = cell.getHeight();

                    if (y < height / 3) {

                        dropPosition = DropPosition.ABOVE;

                    } else if (y > height * 2 / 3) {

                        dropPosition = DropPosition.BELOW;

                    } else {

                        dropPosition = DropPosition.CENTER;
                    }

                    event.acceptTransferModes(TransferMode.MOVE);
                }
                cell.setStyle("");
                switch (dropPosition) {

                case ABOVE ->
                    cell.setStyle("-fx-border-color: dodgerblue; -fx-border-width: 2 0 0 0;");

                case CENTER ->
                    cell.setStyle("-fx-background-color: lightblue;");

                case BELOW ->
                    cell.setStyle("-fx-border-color: dodgerblue; -fx-border-width: 0 0 2 0;");

            }
                
                event.consume();
            });
            cell.setOnDragExited(event -> {

                cell.setStyle("");

            });
            cell.setOnDragDropped(event -> {
            	
            	System.out.println(dropPosition);

                if (!isSortMode.getAsBoolean()) {
                    return;
                }

                Dragboard db = event.getDragboard();

                boolean success = false;
                
                if (dropPosition == DropPosition.CENTER) {

                    System.out.println("親変更");
                    int draggedId = draggedItem.getValue().getId();

                    int newParentId = cell.getItem().getId();
                    System.out.println("dragged = " + draggedId);
                    

                    repository.move(draggedId, newParentId);

                    refreshTree.run();

                } else {

                	TreeItem<NoteNode> targetItem = cell.getTreeItem();

                	TreeItem<NoteNode> parentItem = targetItem.getParent();

                	Integer parentId = null;

                	if (parentItem != null) {
                	    parentId = parentItem.getValue().getId();
                	}

                	List<NoteNode> siblings;

                	if (parentId == null) {
                	    siblings = repository.findChildren(1);
                	} else {
                	    siblings = repository.findChildren(parentId);
                	}

                	int draggedId = draggedItem.getValue().getId();
                	int targetId = targetItem.getValue().getId();

                	// ドラッグしたノート取得
                	NoteNode draggedNote = null;

                	for (NoteNode note : siblings) {

                	    if (note.getId() == draggedId) {

                	        draggedNote = note;
                	        break;
                	    }
                	}

                	if (draggedNote == null) {
                	    return;
                	}

                	// 一旦取り除く
                	siblings.remove(draggedNote);

                	// ターゲット位置を探す
                	int targetIndex = 0;

                	for (int i = 0; i < siblings.size(); i++) {

                	    if (siblings.get(i).getId() == targetId) {

                	        targetIndex = i;
                	        break;
                	    }
                	}

                	// 上下で挿入位置を変える
                	if (dropPosition == DropPosition.BELOW) {
                	    targetIndex++;
                	}

                	siblings.add(targetIndex, draggedNote);

                	// DBへ保存
                	repository.updateSortOrders(siblings);

                	refreshTree.run();

                }

                if (db.hasString()) {

                    System.out.println("ドロップ成功");

                    success = true;
                }

                event.setDropCompleted(success);
                event.consume();
            });
            return cell;
        });
        
    }
}