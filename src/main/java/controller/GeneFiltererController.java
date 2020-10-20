package controller;

import annotation.Gene;
import annotation.Isoform;
import exceptions.InvalidMinFoldChangeCutOff;
import exceptions.InvalidPercentCutOff;
import exceptions.InvalidTPMCutOffException;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import labelset.Cluster;
import labelset.LabelSet;
import mediator.ControllerMediator;
import org.controlsfx.control.CheckComboBox;
import ui.Main;


import java.net.URL;
import java.util.*;

public class GeneFiltererController extends PopUpController implements Initializable {
    private static final double GENE_FILTERER_WIDTH = 500;
    private static final double GENE_FILTERER_HEIGHT = 600;
    // Dominant isoform switching (DIS)
    private static final int DEFAULT_DIS_MIN_TPM = 10;
    private static final int DEFAULT_DIS_MIN_PERCENT_EXPRESSED = 50;
    // Differential Expression (DE)
    private static final int DEFAULT_DE_MIN_FOLD_CHANGE = 10;
    private static final int DEFAULT_DE_MIN_TPM = 10;
    private static final int DEFAULT_DE_MIN_PERCENT_EXPRESSED = 50;
    // Category-specific expression (CSE)
    private static final int DEFAULT_CSE_MIN_TPM = 10;
    private static final int DEFAULT_CSE_MIN_PERCENT_EXPRESSED = 75;
    private static final int DEFAULT_CSE_MAX_TPM = 250;
    private static final int DEFAULT_CSE_MAX_PERCENT_EXPRESSED = 25;

    @FXML private Parent geneFilterer;
    @FXML private ToggleGroup filterToggles;

    @FXML private RadioButton noneFilterOption;
    // Dominant isoform switching (DIS)
    @FXML private RadioButton disFilterOption;
    @FXML private TextField disMinTPMField;
    @FXML private TextField disMinPercentExpressedField;
    // Differential Expression (DE)
    @FXML private RadioButton deFilterOption;
    @FXML private CheckComboBox deCategories;
    @FXML private TextField deMinFoldChangeField;
    @FXML private TextField deMinTPMField;
    @FXML private TextField deMinPercentExpressedField;
    // Category-specific expression (CSE)
    @FXML private RadioButton cseFilterOption;
    @FXML private CheckComboBox cseCategories;
    @FXML private TextField cseMinTPMField;
    @FXML private TextField cseMinPercentExpressedField;
    @FXML private TextField cseMaxTPMField;
    @FXML private TextField cseMaxPercentExpressedField;

    private Toggle optionFilteringBy;
    private LabelSet labelSetCategoriesFrom;

    private MutableDouble disMinTPM;
    private MutableDouble disMinPercentExpressed;

    private MutableDouble deMinFoldChange;
    private MutableDouble deMinTPM;
    private MutableDouble deMinPercentExpressed;

    private MutableDouble cseMinTPM;
    private MutableDouble cseMinPercentExpressed;
    private MutableDouble cseMaxTPM;
    private MutableDouble cseMaxPercentExpressed;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpFieldUpdating();
        setFilteringParamsToDefault();
        setUpWindow();
    }

    public void unfilterGenes() {
        noneFilterOption.setSelected(true);
        optionFilteringBy = noneFilterOption;
        ControllerMediator.getInstance().updateGenesTableFilteringMethod();
    }

    public boolean notFilteringGenes() {
        return noneFilterOption.isSelected();
    }

    public boolean isFilteringByDominantIsoformSwitching() {
        return disFilterOption.isSelected();
    }

    public boolean isFilteringByDifferentialExpression() {
        return deFilterOption.isSelected();
    }

    public boolean isFilteringByCategorySpecificExpression() {
        return cseFilterOption.isSelected();
    }

    public boolean geneHasIsoformSwitches(Gene gene) {
        Map<Cluster, Set<Isoform>> dominantIsoformsPerCluster = new HashMap<>();
        Collection<Cluster> clusters = labelSetCategoriesFrom.getClusters();
        for (Cluster cluster : clusters) {
            for (Isoform isoform : gene.getIsoforms()) {
                if (dominantIsoformsPerCluster.containsKey(cluster)) {
                    Set<Isoform> dominantIsoforms = dominantIsoformsPerCluster.get(cluster);
                    updateClusterDominantIsoforms(cluster, isoform, dominantIsoforms);
                } else {
                    Set<Isoform> dominantIsoforms = new HashSet<>();
                    updateClusterDominantIsoforms(cluster, isoform, dominantIsoforms);
                    if (dominantIsoforms.size() > 0)
                        dominantIsoformsPerCluster.put(cluster, dominantIsoforms);
                }
            }
        }

        Collection<Set<Isoform>> dominantIsoformSets = dominantIsoformsPerCluster.values();
        Iterator<Set<Isoform>> dominantIsoformSetsIterator = dominantIsoformSets.iterator();
        if (dominantIsoformSetsIterator.hasNext()) {
            Set<Isoform> clusterDominantIsoforms = dominantIsoformSetsIterator.next();
            while (dominantIsoformSetsIterator.hasNext()) {
                Set<Isoform> otherClusterDominantIsoforms = dominantIsoformSetsIterator.next();
                if (!clusterDominantIsoforms.equals(otherClusterDominantIsoforms)) {
                    return true;
                }
            }
        }
       return false;
    }

    private void updateClusterDominantIsoforms(Cluster cluster, Isoform isoform, Set<Isoform> dominantIsoforms) {
        double isoformExpression = isoform.getAverageExpressionInCluster(cluster, false, false);
        int isoformNumExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoform.getId(), cluster, false);
        double isoformPercentExpressed = (double) isoformNumExpressingCells / cluster.getCells().size();
        if (isoformExpression >= disMinTPM.doubleValue() && isoformPercentExpressed * 100 >= disMinPercentExpressed.doubleValue()) {
            for (Iterator<Isoform> iterator = dominantIsoforms.iterator(); iterator.hasNext();) {
                Isoform dominantIsoform = iterator.next();
                double dominantIsoformExpression = dominantIsoform.getAverageExpressionInCluster(cluster, false, false);
                double expressionRatio = isoformExpression / dominantIsoformExpression;
                if (expressionRatio < (double) 1/1.1) {
                    return;
                } else if (expressionRatio > 1) {
                    if (expressionRatio > 1.1) {
                        iterator.remove();
                    } else {
                        int dominantIsoformNumExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(dominantIsoform.getId(), cluster, false);
                        double dominantIsoformPercentExpressed = (double) dominantIsoformNumExpressingCells / cluster.getCells().size();

                        if (dominantIsoformPercentExpressed < isoformPercentExpressed)
                            iterator.remove();
                    }
                } else {
                    int dominantIsoformNumExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(dominantIsoform.getId(), cluster, false);
                    double dominantIsoformPercentExpressed = (double) dominantIsoformNumExpressingCells / cluster.getCells().size();

                    if (isoformPercentExpressed < dominantIsoformPercentExpressed)
                        return;
                }
            }
            dominantIsoforms.add(isoform);
        }
    }

    public void updateFilterCellCategories() {
        LabelSet labelSet = ControllerMediator.getInstance().getLabelSetInUse();
        if (labelSetCategoriesFrom != labelSet) {
            deCategories.getCheckModel().clearChecks();
            cseCategories.getCheckModel().clearChecks();
            ObservableList deCategoryItems = deCategories.getItems();
            ObservableList cseCategoryItems = cseCategories.getItems();
            deCategoryItems.clear();
            cseCategoryItems.clear();

            if (labelSet != null) {
                for (Cluster cluster : labelSet.getClusters()) {
                    deCategoryItems.add(cluster.getName());
                    cseCategoryItems.add(cluster.getName());
                }
            }
            labelSetCategoriesFrom = labelSet;
        }
    }
    
    @FXML
    protected void handleFilterButton() {
        optionFilteringBy = filterToggles.getSelectedToggle();
        ControllerMediator.getInstance().updateGenesTableFilteringMethod();
    }

    private void setUpFieldUpdating(){
        disMinTPMField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, disMinTPMField, disMinTPM, ChangedCutOffEvent.CHANGED_TPM_CUTOFF));
        disMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, disMinPercentExpressedField, disMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

        deMinFoldChangeField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, deMinFoldChangeField, deMinFoldChange, ChangedCutOffEvent.CHANGED_MIN_FOLD_CHANGE_CUTOFF));
        deMinTPMField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, deMinTPMField, deMinTPM, ChangedCutOffEvent.CHANGED_TPM_CUTOFF));
        deMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, deMinPercentExpressedField, deMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

        cseMinTPMField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, cseMinTPMField, cseMinTPM, ChangedCutOffEvent.CHANGED_TPM_CUTOFF));
        cseMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, cseMinPercentExpressedField, cseMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));
        cseMaxTPMField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, cseMaxTPMField, cseMaxTPM, ChangedCutOffEvent.CHANGED_TPM_CUTOFF));
        cseMaxPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, cseMaxPercentExpressedField, cseMaxPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

    }

    private void handleChangedFilteringParam(Boolean newValue, TextField field, MutableDouble originalCutOff, ChangedCutOffEvent event) {
        if (!newValue) { //when focus lost
            String newCutOff = field.getText();
            try {
                if (event.equals(ChangedCutOffEvent.CHANGED_TPM_CUTOFF))
                    checkTPMCutOff(newCutOff);
                else if (event.equals(ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF))
                    checkPercentExpressedCutOff(newCutOff);
                else
                    checkMinFoldChange(newCutOff);
                originalCutOff.setValue(Double.parseDouble(newCutOff));
            } catch (Exception e){
                field.setText(Double.toString(originalCutOff.doubleValue()));
                ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
            }
        }
    }

    private void setFilteringParamsToDefault() {
        noneFilterOption.setSelected(true);
        optionFilteringBy = noneFilterOption;

        disMinTPMField.setText(Integer.toString(DEFAULT_DIS_MIN_TPM));
        disMinTPM = new MutableDouble(DEFAULT_DIS_MIN_TPM);
        disMinPercentExpressedField.setText(Integer.toString(DEFAULT_DIS_MIN_PERCENT_EXPRESSED));
        disMinPercentExpressed = new MutableDouble(DEFAULT_DIS_MIN_PERCENT_EXPRESSED);

        deMinFoldChangeField.setText(Integer.toString(DEFAULT_DE_MIN_FOLD_CHANGE));
        deMinFoldChange = new MutableDouble(DEFAULT_DE_MIN_FOLD_CHANGE);
        deMinTPMField.setText(Integer.toString(DEFAULT_DE_MIN_TPM));
        deMinTPM = new MutableDouble(DEFAULT_DE_MIN_TPM);
        deMinPercentExpressedField.setText(Integer.toString(DEFAULT_DE_MIN_PERCENT_EXPRESSED));
        deMinPercentExpressed = new MutableDouble(DEFAULT_DIS_MIN_PERCENT_EXPRESSED);

        cseMinTPMField.setText(Integer.toString(DEFAULT_CSE_MIN_TPM));
        cseMinTPM = new MutableDouble(DEFAULT_CSE_MIN_TPM);
        cseMinPercentExpressedField.setText(Integer.toString(DEFAULT_CSE_MIN_PERCENT_EXPRESSED));
        cseMinPercentExpressed = new MutableDouble(DEFAULT_CSE_MIN_PERCENT_EXPRESSED);
        cseMaxTPMField.setText(Integer.toString(DEFAULT_CSE_MAX_TPM));
        cseMaxTPM = new MutableDouble(DEFAULT_CSE_MAX_TPM);
        cseMaxPercentExpressedField.setText(Integer.toString(DEFAULT_CSE_MAX_PERCENT_EXPRESSED));
        cseMaxPercentExpressed = new MutableDouble(DEFAULT_CSE_MAX_PERCENT_EXPRESSED);
    }

    /**
     * Sets up gene selector window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Gene Filterer");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            if (!optionFilteringBy.isSelected())
                optionFilteringBy.setSelected(true);
            window.hide();
        });
    }

    private void checkTPMCutOff(String tpmCutOff) throws InvalidTPMCutOffException {
        double doubleTPMCutOff;
        try {
            doubleTPMCutOff = Double.parseDouble(tpmCutOff);
        } catch (Exception e) {
            throw new InvalidTPMCutOffException();
        }
        if (doubleTPMCutOff < 0)
            throw new InvalidTPMCutOffException();
    }

    private void checkPercentExpressedCutOff(String percentExpressedCutOff) throws InvalidPercentCutOff {
        double doublePercentExpressedCutOff;
        try {
            doublePercentExpressedCutOff = Double.parseDouble(percentExpressedCutOff);
        } catch (Exception e) {
            throw new InvalidPercentCutOff();
        }
        if (doublePercentExpressedCutOff < 0 || doublePercentExpressedCutOff >100)
            throw new InvalidPercentCutOff();
    }

    private void checkMinFoldChange(String minFoldChange) throws InvalidMinFoldChangeCutOff {
        double doubleMinFoldChange;
        try {
            doubleMinFoldChange = Double.parseDouble(minFoldChange);
        } catch (Exception e) {
            throw new InvalidMinFoldChangeCutOff();
        }

        if (doubleMinFoldChange < 1)
            throw new InvalidMinFoldChangeCutOff();
    }

    private void setWindowSizeAndDisplay() {
        window.setScene(new Scene(geneFilterer, GENE_FILTERER_WIDTH, GENE_FILTERER_HEIGHT));
    }

    private class MutableDouble {
        double value;

        public MutableDouble(double value) {
            this.value = value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public double doubleValue() {
            return value;
        }
    }

    private enum ChangedCutOffEvent {
        CHANGED_TPM_CUTOFF,
        CHANGED_PERCENT_CUTOFF,
        CHANGED_MIN_FOLD_CHANGE_CUTOFF
    }
}