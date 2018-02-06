/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tcero.plantillacrud;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

@TemplateRegistrations({
    @TemplateRegistration(
            folder = "JSP_Servlet",
            displayName = "#PlantillaCRUDWizardIterator_displayName",
            content = "CRUD",
            description = "Description.html",
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "JSP_Servlet",
            content = "beanCRUD",
            scriptEngine = "freemarker",
            targetName = "beanCRUD.java")
})
@NbBundle.Messages("PlantillaCRUDWizardIterator_displayName=Primefaces CRUD")
public final class PlantillaCRUDWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    // Example of invoking this wizard:
    // @ActionID(category="...", id="...")
    // @ActionRegistration(displayName="...")
    // @ActionReference(path="Menu/...")
    // public static ActionListener run() {
    //     return new ActionListener() {
    //         @Override public void actionPerformed(ActionEvent e) {
    //             WizardDescriptor wiz = new WizardDescriptor(new PlantillaCRUDWizardIterator());
    //             // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
    //             // {1} will be replaced by WizardDescriptor.Iterator.name()
    //             wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
    //             wiz.setTitle("...dialog title...");
    //             if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
    //                 ...do something...
    //             }
    //         }
    //     };
    // }
    private int index;

    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
            panels.add(new PlantillaCRUDWizardPanel1());
            // Change to default new file panel and add our panel at bottom
            Project p = Templates.getProject(wizard);
            SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
            String[] steps = new String[panels.size()];
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
        }
        return panels;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    @Override
    public Set instantiate() throws IOException {

        FileObject createdFile = null;
        // Read Title from wizard 
        String titulo = (String) wizard.getProperty(PlantillaCRUDVisualPanel1.TITLE);
        String clase = (String) wizard.getProperty(PlantillaCRUDVisualPanel1.CLASE);

        // FreeMarker Template will get its variables from HashMap.
        // HashMap key is the variable name.
        Map<String, String> args = new HashMap<String, String>();
        args.put("title", titulo);
        args.put("clase", clase);
        args.put("listado", "#{bean" + clase + "." + clase.substring(0, 1).toLowerCase()
                + clase.substring(1, clase.length()) + "}");
        args.put("setearlistado", "#{bean" + clase + ".set" + clase + "seleccionado(Item)}");
        args.put("borrar", "#{bean" + clase + ".borrar()}");
        args.put("crear", "#{bean" + clase + ".crear()}");
        args.put("clasevar", clase.substring(0, 1).toLowerCase() + clase.substring(1, clase.length()));
        args.put("beanclaseseleccionado", "#{bean" + clase + ".masterseleccionado}");
        args.put("beanclaselimpiar", "#{bean" + clase + ".limpiar" + clase + "()}");

        //Get the template and convert it:
        FileObject template = Templates.getTemplate(wizard);
        FileObject firstTemplate = null;
        FileObject secondTemplate = null;
        for (FileObject tem : template.getParent().getChildren()) {
            if (tem.getName().equals("CRUD")) {
                firstTemplate = tem;
            }
            if (tem.getName().equals("beanCRUD")) {
                secondTemplate = tem;
            }
        }

        DataObject dTemplate = DataObject.find(firstTemplate);
        DataObject dTemplate2 = DataObject.find(secondTemplate);

        //Get the package.
        FileObject dir1 = Templates.getTargetFolder(wizard);
        FileObject dir2 = null;
        FileObject javafolder = null;
        FileObject carpetaBusca = Templates.getTargetFolder(wizard);
        while (javafolder == null) {
            FileObject[] buscaListado = carpetaBusca.getParent().getChildren();
            carpetaBusca = carpetaBusca.getParent();
            for (FileObject files : buscaListado) {
                if (files.getName().equals("java")) {
                    javafolder = files;
                    for (FileObject carpetajava : files.getChildren()) {
                        if (carpetajava.getName().equals("bean")) {
                            dir2 = carpetajava;
                        }
                    }
                    if (dir2 == null) {
                        dir2 = files.createFolder("bean");
                    }
                }
            }
        }
        DataFolder df1 = DataFolder.findFolder(dir1);
        DataFolder df2 = DataFolder.findFolder(dir2);

        //Get the class:
        String targetName = clase + ".xhtml";
        String targetName2 = "Bean" + clase + ".java";

        //Define the template from the above,
        //passing the package, the file name, and the map of strings to the template:
        DataObject dobj = dTemplate.createFromTemplate(df1, targetName, args);
        dTemplate2.createFromTemplate(df2, targetName2, args);

        //Obtain a FileObject:
        createdFile = dobj.getPrimaryFile();

        // Return the created file.
        return Collections.singleton(createdFile);
    }

    @Override
    public void initialize(WizardDescriptor wd) {
        wizard = wd;
    }

    @Override
    public void uninitialize(WizardDescriptor wd) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
