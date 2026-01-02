package com.devmaster.goatfarm.integration;

import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(EventDao.class)
public class EventDaoIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private EventRepository eventRepository;

    @Test
    public void whenSaveEvent_thenEventIsSaved() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setCpf("12345678900");
        user.setPassword("password");
        entityManager.persist(user);

        GoatFarm farm = new GoatFarm();
        farm.setName("Test Farm");
        farm.setUser(user);
        entityManager.persist(farm);

        Event event = new Event();
        event.setDescription("Test Event");
        // event.setGoat(goat); // Supondo que a entidade Goat também seja criada e persistida
        
        Event savedEvent = eventDao.saveEvent(event);
        
        assertThat(savedEvent).isNotNull();
        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getDescription()).isEqualTo("Test Event");
    }
}


