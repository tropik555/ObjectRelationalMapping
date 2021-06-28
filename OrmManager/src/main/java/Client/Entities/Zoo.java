package Client.Entities;

import OrmArchivarius.Annotations.Column;
import OrmArchivarius.Annotations.Entity;
import OrmArchivarius.Annotations.Id;

@Entity
public class Zoo {
    @Id
    @Column
    Long id;

    @Column
    String address;

    public Zoo() {
    }



    public Zoo(String address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Zoo{" +
                "id=" + id +
                ", address=" + address +

                '}';
    }
}
