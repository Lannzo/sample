package appdev.com.techmatch.controller;

import appdev.com.techmatch.model.Event;
import appdev.com.techmatch.model.Topic;
import appdev.com.techmatch.service.EventService;
import appdev.com.techmatch.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import appdev.com.techmatch.repository.TopicRepository;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @Autowired
    private TopicRepository topicRepository;

    @PostMapping("/create")
    public String createEvent(
        @ModelAttribute Event event,
        @RequestParam("eventTopics") String[] eventTopics, // Collect selected topics
        @RequestParam("imageFile") MultipartFile imageFile,
        @RequestParam("userID") String userID
    ) throws IOException {
        // Combine selected topics into a comma-separated string
    
        // Link the user to the event
        User user = new User();
        user.setUserID(userID);
        event.setUser(user);
    
        // Process the uploaded image
        if (!imageFile.isEmpty()) {
            event.setEventImage(imageFile.getBytes());
        }


        // Process event topics
        for (String topicName : eventTopics) {
            // Check if topic already exists
            Topic topic = topicRepository.findByName(topicName);
            if (topic == null) {
                // Create a new topic if it doesn't exist
                topic = new Topic();
                topic.setName(topicName);
                topicRepository.save(topic);
            }
            event.getTopics().add(topic);
        }

        eventService.saveEvent(event);
    
    
        return "redirect:/home";
    }

@GetMapping("/{id}") // Change from /events/{id} to simply /{id}
@ResponseBody
public Map<String, Object> getEventDetails(@PathVariable String id) {
    Event event = eventService.getEventById(id);

    if (event == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
    }

    Map<String, Object> response = new HashMap<>();
    response.put("eventName", event.getEventName());
    response.put("description", event.getDescription());
    response.put("location", event.getLocation());
    response.put("startDate", event.getStartDate());
    response.put("endDate", event.getEndDate());
    response.put("eventType", event.getEventType());
    response.put("topics", event.getTopics().stream().map(Topic::getName).toList());

    return response;
}

@GetMapping("/event/{id}")
public String getEventDetails(@PathVariable("id") String id, Model model) {
    try {
        Event event = eventService.getEventById(id);
        model.addAttribute("event", event);
    } catch (Exception e) {
        System.err.println("Error fetching event details: " + e.getMessage());
        return "redirect:/home"; // Redirect to home if event not found
    }
    return "event-details";
}

    @GetMapping("/create")
    public String showEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "create-event"; // Render the event creation form
    }

    @GetMapping("/events")
    @ResponseBody
    public List<Event> getFilteredEvents(
        @RequestParam(value = "topic", required = false) String topic,
        @RequestParam(value = "date", required = false) String date,
        @RequestParam(value = "type", required = false) String type
    ) {
        System.out.println("Filter Params - Topic: " + topic + ", Date: " + date + ", Type: " + type);

        if (topic != null) {
            return eventService.getEventsByTopic(topic);
        } else if (date != null || type != null) {
            return eventService.getEventsByDateAndType(date, type);
        } else {
            return eventService.getAllEvents();
        }
    }

}

