library(ggplot2)
library(dplyr)
library(tidyr)
library(gridExtra)

################################################################################
# Reads and processes output logs from GLM, BDMM and BayesMLP packages for BEAST2.
# The runs have to be done already. Set path below to their dir. 
# File names may need midification. 
#Current name: "test_"+ model + hidden layers (if applicable) + random seed +".log"
################################################################################

# Set paths
wd <- "~/Documents/Source/beast2.7/NNvsGLM/testing"
log_dir <- "out_logs"
out_dir <-"out_figs"

#True sampling rate values
true_values <- c(0.5, 1.5, 2)

setwd(wd)

# Read BEAST2 log files
read_beast_log <- function(file_path) {
  data <- read.table(file_path, header = TRUE, comment.char = "#", sep = "\t", fill = TRUE)
  data <- data %>% select(matches("^samplingRateSVi[0-9]$"))  # Select only relevant parameters, excluding changeTime
  data$Source <- sub("^[^_]+_(.*?)_\\d+\\.log$", "\\1", basename(file_path))  # Extract method name, ignoring random seed
  return(data)
}


setwd(paste0(wd,"/",log_dir))

# Define file pattern to match all relevant log files
file_patterns <- c("test_GLM_*.log", "test_NN_*.log", "test_BDMM_*.log")  # Adjust patterns as needed

# Get list of files dynamically
log_files <- unlist(lapply(file_patterns, Sys.glob))

# Read log files
log_data_list <- lapply(log_files, read_beast_log)


# Standardize column names (some logs may have missing columns)
common_cols <- Reduce(intersect, lapply(log_data_list, colnames))
log_data_list <- lapply(log_data_list, function(df) df %>% select(all_of(common_cols)))

# Bind all data
log_data <- bind_rows(log_data_list)

# Convert to long format
sampling_data <- log_data %>%
  pivot_longer(cols = starts_with("samplingRateSVi"), names_to = "Parameter", values_to = "Value")

# Plot posterior distributions for a specific parameter
plot_posterior <- function(data, param, true_value) {
  ggplot(data %>% filter(Parameter == param), aes(x = Value, fill = Source)) +
    geom_density(alpha = 0.5) +
    geom_vline(xintercept = true_value, linetype = "dashed", color = "red") +
    labs(title = paste("Posterior Distribution of", param),
         x = "Value",
         y = "Density",
         fill = "Log File") +
    theme_minimal()
}

setwd(paste0(wd,"/",out_dir))
# Function that saves plots
save_plot <- function(plot, filename) {
  ggsave(filename, plot, width = 8, height = 6, dpi = 300)
}

# Print plots
plot_svi0 <- plot_posterior(sampling_data, "samplingRateSVi0", true_values[1])
plot_svi1 <- plot_posterior(sampling_data, "samplingRateSVi1", true_values[2])
plot_svi2 <- plot_posterior(sampling_data, "samplingRateSVi2", true_values[3])

# Save all separately 
save_plot(plot_svi0, "posterior_samplingRateSVi0.png")
save_plot(plot_svi1, "posterior_samplingRateSVi1.png")
save_plot(plot_svi2, "posterior_samplingRateSVi2.png")

# Save all plots in one PDF
pdf("posterior_distributions.pdf", width = 10, height = 12)
grid.arrange(plot_svi0, plot_svi1, plot_svi2, ncol = 1)
dev.off()

# Violin plot with all methods and parameters
plot <- ggplot(sampling_data, aes(x = Parameter, y = Value, fill = Source)) +
  geom_violin(alpha = 0.7, scale="width") +
  geom_hline(yintercept = true_values, linetype = "dashed", color = "red") +
  labs(title = "Comparison of Posterior Distributions for Different Methods",
       x = "Parameter",
       y = "Estimated Values",
       fill = "Method") +
  theme_minimal()

# Save violin plot
ggsave("violin_comparison.pdf", plot, width = 10, height = 6, dpi = 300)

