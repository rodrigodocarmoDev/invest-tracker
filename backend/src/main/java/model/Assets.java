package model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "assets")
public class Assets {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, unique = true)
    public String ticker;

    @Column(nullable = false)
    public String type;

    @Column(nullable = false)
    public String name;
}
