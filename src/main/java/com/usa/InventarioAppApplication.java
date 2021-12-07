package com.usa;

import com.usa.controlador.Controlador;
import com.usa.modelo.RepositorioProducto;
import com.usa.vista.VentanaModificar;
import com.usa.vista.VentanaPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication
@ComponentScan("com.usa.modelo")
@EnableJdbcRepositories("com.usa.modelo")
public class InventarioAppApplication {
        @Autowired
        RepositorioProducto repositorio;
	public static void main(String[] args) {
		//SpringApplication.run(InventarioAppApplication.class, args);
                SpringApplicationBuilder builder = new SpringApplicationBuilder(InventarioAppApplication.class);
                builder.headless(false);
                ConfigurableApplicationContext context = builder.run(args);                
	}
        
        @Bean
        ApplicationRunner applicationRunner() {
            return args ->{
            VentanaPrincipal ventanaPrin = new VentanaPrincipal();
            VentanaModificar ventanaMod = new VentanaModificar();
            Controlador controlador = new Controlador(repositorio, ventanaPrin, ventanaMod);
            
            };
        }
        

}
