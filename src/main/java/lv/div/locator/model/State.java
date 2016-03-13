package lv.div.locator.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Current state for every connected device
 */
@Entity
@Table(name = "state",
    uniqueConstraints = @UniqueConstraint(name = "unq_state_key", columnNames = {"deviceid", "ckey"})
)
public class State extends ConfigurationBased {

    public State() {
    }
}
