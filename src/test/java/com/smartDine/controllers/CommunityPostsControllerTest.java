package com.smartDine.controllers;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.smartDine.dto.community.post.CommunityPostResponseDTO;
import com.smartDine.dto.community.post.CommunityPostSummaryDTO;
import com.smartDine.entity.Customer;
import com.smartDine.entity.User;
import com.smartDine.services.CommunityPostService;

@ExtendWith(MockitoExtension.class)
public class CommunityPostsControllerTest {

    @Mock
    private CommunityPostService communityPostService;

    @InjectMocks
    private CommunityPostsController communityPostsController;

    private MockMvc mockMvc;
    private Customer user;

    @BeforeEach
    void setup() {
        user = new Customer();
        user.setId(5L);
        user.setName("Tester");
        user.setEmail("tester@example.com");
        user.setPassword("pass");
        user.setPhoneNumber(123L);

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

    @Test
    void createPostShouldReturnOk() throws Exception {
        CommunityPostResponseDTO responseDTO = new CommunityPostResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Title");
        when(communityPostService.createPost(anyLong(), any())).thenReturn(responseDTO);

        mockMvc.perform(post("/smartdine/api/communities/1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"communityId\":1,\"title\":\"Title\",\"description\":\"Body\"}"))
                .andExpect(status().isOk());

        verify(communityPostService).createPost(anyLong(), any());
    }

    @Test
    void getPostsByMemberShouldReturnOk() throws Exception {
        CommunityPostSummaryDTO summary = new CommunityPostSummaryDTO();
        summary.setId(2L);
        
        // Create a proper PageImpl with Pageable to avoid serialization issues
        Pageable pageable = PageRequest.of(0, 5);
        when(communityPostService.getPostsByMember(anyLong(), any(), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(summary), pageable, 1));

        mockMvc.perform(get("/smartdine/api/communities/members/3/posts")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(communityPostService).getPostsByMember(anyLong(), any(), any(Pageable.class), any());
    }

    @Test
    void deletePostShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/smartdine/api/communities/posts/4"))
                .andExpect(status().isNoContent());

        verify(communityPostService).deletePost(anyLong(), anyLong());
    }
}
