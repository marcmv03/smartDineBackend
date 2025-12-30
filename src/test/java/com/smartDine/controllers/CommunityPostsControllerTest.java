package com.smartDine.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.smartDine.entity.Community;
import com.smartDine.entity.CommunityType;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.User;
import com.smartDine.entity.community.CommunityPost;
import com.smartDine.entity.community.OpenReservationPost;
import com.smartDine.services.CommunityPostService;

@ExtendWith(MockitoExtension.class)
public class CommunityPostsControllerTest {

    @Mock
    private CommunityPostService communityPostService;

    @InjectMocks
    private CommunityPostsController communityPostsController;

    private MockMvc mockMvc;
    private Customer user;
    private Community community;
    private Member member;

    @BeforeEach
    void setup() {
        user = new Customer();
        user.setId(5L);
        user.setName("Tester");
        user.setEmail("tester@example.com");
        user.setPassword("pass");
        user.setPhoneNumber(123L);

        community = new Community();
        community.setId(1L);
        community.setName("Test Community");
        community.setDescription("A test community");
        community.setVisibility(true);
        community.setCommunityType(CommunityType.USER);

        member = new Member();
        member.setId(10L);
        member.setUser(user);
        member.setCommunity(community);
        member.setMemberRole(MemberRole.ADMIN);

        // Custom argument resolver that returns our test user for @AuthenticationPrincipal
        HandlerMethodArgumentResolver customResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) &&
                       User.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return user;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(communityPostsController)
                .setCustomArgumentResolvers(customResolver)
                .build();
    }

    private CommunityPost createTestPost(Long id, String title, String description) {
        CommunityPost post = new CommunityPost();
        post.setId(id);
        post.setTitle(title);
        post.setDescription(description);
        post.setCommunity(community);
        post.setAuthor(member);
        post.setPublishedAt(LocalDateTime.now());
        return post;
    }

    @Test
    void createPostShouldReturnOk() throws Exception {
        CommunityPost post = createTestPost(1L, "Title", "Body");
        when(communityPostService.createPost(anyLong(), any())).thenReturn(post);

        mockMvc.perform(post("/smartdine/api/communities/1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":1,\"title\":\"Title\",\"description\":\"Body\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postType").exists())
                .andExpect(jsonPath("$.postType").value("NORMAL"));

        verify(communityPostService).createPost(anyLong(), any());
    }

    @Test
    void getPostsByMemberShouldReturnOk() throws Exception {
        CommunityPost post = createTestPost(2L, "Test Post", "Test Description");
        
        when(communityPostService.getPostsByMember(anyLong(), any(), any()))
                .thenReturn(List.of(post));

        // getPostsByMember returns CommunityPostSummaryDTO which doesn't include postType
        mockMvc.perform(get("/smartdine/api/communities/members/3/posts"))
                .andExpect(status().isOk());

        verify(communityPostService).getPostsByMember(anyLong(), any(), any());
    }

    @Test
    void deletePostShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/smartdine/api/communities/posts/4"))
                .andExpect(status().isNoContent());

        verify(communityPostService).deletePost(anyLong(), anyLong());
    }

    @Test
    void getOpenReservationPostShouldReturnCorrectPostType() throws Exception {
        // Create an OpenReservationPost
        OpenReservationPost openPost = new OpenReservationPost();
        openPost.setId(5L);
        openPost.setTitle("Open Reservation");
        openPost.setDescription("Join our dinner!");
        openPost.setCommunity(community);
        openPost.setAuthor(member);
        openPost.setPublishedAt(LocalDateTime.now());
        openPost.setMaxParticipants(5);
        openPost.setCurrentParticipants(2);
        // The constructor already sets type to OPEN_RESERVATION

        when(communityPostService.getOpenReservationPostById(anyLong(), any()))
                .thenReturn(openPost);

        // OpenReservationPostResponseDTO uses PostType enum, serialized as string
        mockMvc.perform(get("/smartdine/api/communities/openreservationposts/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.type").value("OPEN_RESERVATION"));

        verify(communityPostService).getOpenReservationPostById(anyLong(), any());
    }
}
