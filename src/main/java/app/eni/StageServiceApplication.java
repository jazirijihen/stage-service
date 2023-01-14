package app.eni;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
class Stage{
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	String sujet; 
	String description;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Long etudiantID;
	@Transient
	private Etudiant etudiant;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Long encadrantID;
	@Transient
	private Encadrant encadrant;
	private String EncadrantName;
	private String EtudiantName;
	private String entreprise;
    private String debutDate ;
    private String finalDate;


}
@RepositoryRestResource
interface StageRepository extends JpaRepository<Stage,Long>{}
@Projection(name="fullStage",types=Stage.class)
interface StageProjection{
	public Long getId();
	public String getSujet();
	public Long getEtudiantID();
}

@Data
class Etudiant{
	private Long id; private String name; private String email;
}
@Data
class Encadrant{
	private Long id; private String name; private String email;private String available;
	

}
@FeignClient(name="ETUDIANT-SERVICE")
interface EtudiantService{
	@GetMapping("/etudiants/{id}")
	public Etudiant findEtudiantById(@PathVariable(name="id") Long id);
	
}
@FeignClient(name="ENCADRANT-SERVICE")
interface EncadrantService{
	@GetMapping("/encadrants/{id}")
	public Encadrant findEncadrantById(@PathVariable(name="id") Long id);

}
@SpringBootApplication
@EnableFeignClients
public class StageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StageServiceApplication.class, args);
	}


}
@Controller
@RequestMapping("/stage/")

class StageRestController{
	@Autowired
	private StageRepository stageRepository;
	@Autowired
	private EncadrantService encadrantService;
	@Autowired
	private EtudiantService etudiantService;

	@GetMapping("list")
    public String listStages(Model model) {
    	
    	
    	List<Stage> le = (List<Stage>) stageRepository.findAll();
    	if(le.size()==0) le = null;
        model.addAttribute("stages", le);       
        return "stage/listStages";
        
       
    }
    
    @GetMapping("add")
    public String showAddStageForm(Model model) {
    	Stage stage = new Stage();// object dont la valeur des attributs par defaut
   
    	model.addAttribute("stage", stage);
        return "stage/addStage";
    }
    
    @PostMapping("add")
    public String addStage( Stage stage, BindingResult result) {
        if (result.hasErrors()) {
            return "stage/addStage";
        }
        Etudiant e =etudiantService.findEtudiantById(stage.getEtudiantID());
        stage.setEtudiantName(e.getName());
        Encadrant en =encadrantService.findEncadrantById(stage.getEncadrantID());
        stage.setEncadrantName(en.getName());
        stageRepository.save(stage);
        return "redirect:list";
    }

    
    @GetMapping("delete/{id}")
    public String deleteStage(@PathVariable("id") long id, Model model) {

    	Stage stage = stageRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("l'Id de Stage est invalide:" + id));

    	stageRepository.delete(stage);

        return "redirect:../list";
    }
    
    
    @GetMapping("edit/{id}")
    public String showStageFormToUpdate(@PathVariable("id") long id, Model model) {
    	Stage stage = stageRepository.findById(id)
            .orElseThrow(()->new IllegalArgumentException("l'Id de Stage est invalide:" + id));
        
        model.addAttribute("stage", stage);
        
        return "stage/updateStage";
    }


    
    @PostMapping("update")
    public String updateStage( Stage stage, BindingResult result) {
    	if (result.hasErrors()) {
            return "stage/updateStage";
        }
    	Etudiant e =etudiantService.findEtudiantById(stage.getEtudiantID());
        stage.setEtudiantName(e.getName());
        Encadrant en =encadrantService.findEncadrantById(stage.getEncadrantID());
        stage.setEncadrantName(en.getName());
    	stageRepository.save(stage);
    	return"redirect:list";
    	
    }
    
    @GetMapping("show/{id}")
	public String showStage(@PathVariable("id") long id, Model model) {
    	Stage stage = stageRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("l'Id de Stage est invalide:" + id));
		model.addAttribute("stage", stage);
		return "stage/showStage";
	}
}