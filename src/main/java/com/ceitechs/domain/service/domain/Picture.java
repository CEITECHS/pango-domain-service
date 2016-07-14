package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class Picture {
    private String extension;
    private String contentBase64;
    private String caption;
}
