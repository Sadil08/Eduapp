package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
        name = "cart_bundles",
        joinColumns = @JoinColumn(name = "cart_id"),
        inverseJoinColumns = @JoinColumn(name = "bundle_id")
    )
    private List<PaperBundle> bundles = new ArrayList<>();

    public Cart() {}

    public Cart(User user) {
        this.user = user;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<PaperBundle> getBundles() { return bundles; }
    public void setBundles(List<PaperBundle> bundles) { this.bundles = bundles; }
}