/*
 * SessionConsoleProcessTable.hpp
 *
 * Copyright (C) 2009-17 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#ifndef SESSION_CONSOLE_PROCESS_TABLE_HPP
#define SESSION_CONSOLE_PROCESS_TABLE_HPP

#include <session/SessionConsoleProcess.hpp>

namespace rstudio {
namespace core {
   class Error;
}
}

namespace rstudio {
namespace session {
namespace console_process {

// ------------------------------------------------------------------------
// APIs to manipulate and query the list of known ConsoleProcess objects.
// ------------------------------------------------------------------------

// Find a proc
ConsoleProcessPtr findProcByHandle(const std::string& handle);
ConsoleProcessPtr findProcByCaption(const std::string& caption);

// Last-activated proc in the client
ConsoleProcessPtr getVisibleProc();
void clearVisibleProc();
void setVisibleProc(const std::string& handle);

// Build a list of all process captions (captions are used as unique IDs by
// the Terminal R API)
std::vector<std::string> getAllCaptions();

// Determine next terminal sequence number and default name
std::pair<int, std::string> nextTerminalName();

// Get list of all process metadata
core::json::Array allProcessesAsJson();

// Persist the list of ConsoleProcesses.
void saveConsoleProcesses();
void saveConsoleProcessesAtShutdown(bool terminatedNormally);

// Add a ConsoleProcess to table, tracked by handle
void addConsoleProcess(const ConsoleProcessPtr& proc);

// Delete a ConsoleProcess from table and delete its buffer cache.
core::Error reapConsoleProcess(const ConsoleProcess& proc);

// Initialize ConsoleProcess list and APIs
core::Error internalInitialize();

// Create a ConsoleProcess and add it to the table. Final parameter returns handle.
core::Error createTerminalConsoleProc(
      TerminalShell::TerminalShellType shellType,
      int cols,
      int rows,
      const std::string& termHandle, // empty if starting a new terminal
      const std::string& termCaption,
      const std::string& termTitle,
      int termSequence,
      bool altBufferActive,
      std::string currentDir,
      bool zombie,
      bool trackEnv,
      std::string* pHandle);

} // namespace console_process
} // namespace session
} // namespace rstudio

#endif // SESSION_CONSOLE_PROCESS_TABLE_HPP

